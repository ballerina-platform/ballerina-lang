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

# The hopping window releases the events in batches defined by a time period every given time interval. The batch is
# also determined by  the time period given in the window. When the time interval the events being released and the
# time period it hold the events are equal, the hopping window acts as a `TimeBatch` window.
# E.g.
#       from inputStream window `hopping(5000, 4000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# Hopping window should only have two parameters (<int> windowTime, <int> hoppingTime)
#
# + timeInMilliSeconds - description
# + hoppingTime - description
# + windowParameters - description
# + nextEmitTime - description
# + currentEventQueue - description
# + resetEvent - description
# + nextProcessPointer - description
# + scheduler - description
public type HoppingWindow object {
    *Window;
    *Snapshotable;
    public int timeInMilliSeconds;
    public int hoppingTime;
    public any[] windowParameters;
    public int nextEmitTime = -1;
    public LinkedList currentEventQueue;
    public StreamEvent? resetEvent;
    public function (StreamEvent?[])? nextProcessPointer;
    public Scheduler? scheduler = ();

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMilliSeconds = 0;
        self.hoppingTime = 0;
        self.resetEvent = ();
        self.currentEventQueue = new();
        self.initParameters(self.windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });
    }

    public function getScheduler() returns Scheduler {
        return <Scheduler> self.scheduler;
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 2) {
            any windowTimeParam = parameters[0];
            if (windowTimeParam is int) {
                self.timeInMilliSeconds = windowTimeParam;
            } else {
                error err = error("Hopping window's first parameter, windowTime should be of type int");
                panic err;
            }

            any hoppingTimeParam = parameters[1];
            if (hoppingTimeParam is int) {
                self.hoppingTime = hoppingTimeParam;
            } else {
                error err = error("Hopping window's second parameter, hoppingTime should be of type int");
                panic err;
            }
        } else {
            error err = error("Hopping window should only have two parameters (<int> windowTime, <int> " +
                "hoppingTime), but found " + parameters.length().toString() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList outputStreamEvents = new();
        if (self.nextEmitTime == -1) {
            self.nextEmitTime = time:currentTime().time + self.hoppingTime;
            self.getScheduler().notifyAt(self.nextEmitTime);
        }

        int currentTime = time:currentTime().time;
        boolean sendEvents = false;

        if (currentTime >= self.nextEmitTime) {
            self.nextEmitTime += self.hoppingTime;
            self.getScheduler().notifyAt(self.nextEmitTime);
            sendEvents = true;
        } else {
            sendEvents = false;
        }

        foreach var evt in streamEvents {
            StreamEvent event = <StreamEvent>evt;
            if (event.eventType != "CURRENT") {
                continue;
            }
            StreamEvent clonedEvent = event.copy();
            self.currentEventQueue.addLast(clonedEvent);
        }
        if (sendEvents) {
            if (!(self.currentEventQueue.getFirst() is ())) {
                if (!(self.resetEvent is ())) {
                    outputStreamEvents.addLast(self.resetEvent);
                    self.resetEvent = ();
                }
                self.resetEvent = createResetStreamEvent(getStreamEvent(self.currentEventQueue.getFirst()));
                self.currentEventQueue.resetToFront();
                while (self.currentEventQueue.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(self.currentEventQueue.next());
                    if (streamEvent.timestamp >= self.nextEmitTime - self.hoppingTime - self
                        .timeInMilliSeconds && streamEvent.timestamp < self.nextEmitTime - self.hoppingTime) {
                        outputStreamEvents.addLast(streamEvent);
                    } else {
                        self.currentEventQueue.removeCurrent();
                    }
                }
            }
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (outputStreamEvents.getSize() != 0) {
                StreamEvent?[] events = [];
                outputStreamEvents.resetToFront();
                while (outputStreamEvents.hasNext()) {
                    StreamEvent streamEvent = getStreamEvent(outputStreamEvents.next());
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
                        public boolean isLHSTrigger = true)
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
            "nextEmitTime": self.nextEmitTime
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
        any n = state["nextEmitTime"];
        if (n is int) {
            self.nextEmitTime = n;
        }
    }
};

# The `hopping` function creates a `HoppingWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function hopping(any[] windowParameters, public function (StreamEvent?[])? nextProcessPointer = ())
                                                                                                                                                                                                                                                                                                                                                                                                              returns Window {
    HoppingWindow hoppingWindow = new(nextProcessPointer, windowParameters);
    return hoppingWindow;
}
