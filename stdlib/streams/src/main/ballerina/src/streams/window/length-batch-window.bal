// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.
import ballerina/time;

# This is a batch (tumbling) length window, that holds up to the given length of events, and gets updated on every
# given number of events arrival.
# E.g.
#       from inputStream window `lengthBatch(5)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `lengthBatch` window should only have one parameter (<int> windowBatchLength)
#
# + length - description
# + windowParameters - description
# + count - description
# + resetEvent - description
# + currentEventQueue - description
# + nextProcessPointer - description
public type LengthBatchWindow object {
    *Window;
    *Snapshotable;
    public int length;
    public any[] windowParameters;
    public int count;
    public StreamEvent? resetEvent;
    public LinkedList currentEventQueue;
    public (function (StreamEvent?[]))? nextProcessPointer;

    public function __init((function (StreamEvent?[]))? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.length = 0;
        self.count = 0;
        self.resetEvent = ();
        self.currentEventQueue = new();
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any parameter0 = parameters[0];
            if (parameter0 is int) {
                self.length = parameter0;
            } else {
                error err = error("LengthBatch window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("LengthBatch window should only have one parameter (<int> " +
                "windowBatchLength), but found " + parameters.length().toString() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunks = new();
        LinkedList outputStreamEventChunk = new();
        int currentTime = time:currentTime().time;

        foreach var evt in streamEvents {
            StreamEvent event = <StreamEvent>evt;
            StreamEvent clonedStreamEvent = event.copy();
            self.currentEventQueue.addLast(clonedStreamEvent);
            self.count += 1;
            if (self.count == self.length) {
                if (!(self.currentEventQueue.getFirst() is ())) {
                    if (!(self.resetEvent is ())) {
                        outputStreamEventChunk.addLast(self.resetEvent);
                        self.resetEvent = ();
                    }
                    StreamEvent firstInCurrentEventQueue = getStreamEvent(self.currentEventQueue.getFirst());
                    self.resetEvent = createResetStreamEvent(firstInCurrentEventQueue);
                    foreach var currentEvent in self.currentEventQueue.asArray() {
                        outputStreamEventChunk.addLast(currentEvent);
                    }
                }
                self.currentEventQueue.clear();
                self.count = 0;
                if (!(outputStreamEventChunk.getFirst() is ())) {
                    streamEventChunks.addLast(outputStreamEventChunk);
                }
            }
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            streamEventChunks.resetToFront();
            while streamEventChunks.hasNext() {
                StreamEvent?[] events = [];
                LinkedList streamEventChunk;
                any next = streamEventChunks.next();
                if (next is LinkedList) {
                    streamEventChunk = next;
                } else {
                    return;
                }
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                    events[events.length()] = streamEvent;
                }
                nextProcessFuncPointer(events);
            }
        }
    }

    # Returns the events(State) which match with the where condition in the join clause for a given event.
    # + originEvent - The event against which the state or the events being held by the window is matched.
    # + conditionFunc - The function pointer to the lambda function which contain the condition logic in where clause.
    # + isLHSTrigger - Specify if the join is triggered when the lhs stream received the events, if so it should be
    #                  true. Most of the time it is true. In rare cases, where the join is triggered when the rhs
    #                   stream receives events this should be false.
    # + return - Returns an array of 2 element tuples of events. A tuple contains the matching events one from lhs
    #            stream and one from rhs stream.
    public function getCandidateEvents(
                        StreamEvent originEvent,
                        (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? conditionFunc,
                        boolean isLHSTrigger = true)
                        returns @tainted [StreamEvent?, StreamEvent?][] {
        [StreamEvent?, StreamEvent?][] events = [];
        int i = 0;
        foreach var e in self.currentEventQueue.asArray() {
            if (e is StreamEvent) {
                StreamEvent lshEvent = (isLHSTrigger) ? originEvent : e;
                StreamEvent rhsEvent = (isLHSTrigger) ? e : originEvent;

                if (conditionFunc is function (map<anydata> e1Data, map<anydata> e2Data) returns boolean) {
                    if (conditionFunc(lshEvent.data, rhsEvent.data)) {
                        events[i] = [lshEvent, rhsEvent];
                        i += 1;
                    }
                } else {
                    events[i] = [lshEvent, rhsEvent];
                    i += 1;
                }
            }
        }
        return events;
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] currentEventsList = toSnapshottableEvents(self.currentEventQueue.asArray());
        StreamEvent? resetStreamEvt = self.resetEvent;
        SnapshottableStreamEvent? resetEvt = (resetStreamEvt is StreamEvent)
        ? toSnapshottableEvent(resetStreamEvt) : ();
        return {
            "currentEventsList": currentEventsList,
            "resetEvt": resetEvt,
            "count": self.count
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var currentEventsList = state["currentEventsList"];
        if (currentEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] currentEvents = toStreamEvents(currentEventsList);
            self.currentEventQueue = new;
            self.currentEventQueue.addAll(currentEvents);
        }
        var resetEvt = state["resetEvt"];
        if (resetEvt is SnapshottableStreamEvent) {
            StreamEvent r = toStreamEvent(resetEvt);
            self.resetEvent = r;
        }
        any c = state["count"];
        if (c is int) {
            self.count = c;
        }
    }
};

# The `lengthBatch` function creates a `LengthBatchWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function lengthBatch(any[] windowParameters, public function (StreamEvent?[])? nextProcessPointer = ())
                                                                                                                                                                                                                                                                                                                                                                                                                      returns Window {
    LengthBatchWindow lengthBatchWindow = new(nextProcessPointer, windowParameters);
    return lengthBatchWindow;
}
