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
import ballerina/task;

# This is a batch (tumbling) time window, that holds events arrived between window time periods, and gets updated for
# every window time.
# E.g.
#       from inputStream window `timeBatch(5000)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `timeBatch` window should only have one parameter (<int> windowBatchTime)
#
# + timeInMilliSeconds - description
# + windowParameters - description
# + currentEventQueue - description
# + resetEvent - description
# + nextProcessPointer - description
# + scheduler - description
public type TimeBatchWindow object {
    *Window;
    *Snapshotable;
    public int timeInMilliSeconds;
    public any[] windowParameters;
    public LinkedList currentEventQueue;
    public StreamEvent? resetEvent;
    public function (StreamEvent?[])? nextProcessPointer;
    public task:Scheduler scheduler;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMilliSeconds = 0;
        self.resetEvent = ();
        self.currentEventQueue = new();
        self.initParameters(self.windowParameters);
        self.scheduler = new({
            intervalInMillis: self.timeInMilliSeconds,
            initialDelayInMillis: self.timeInMilliSeconds
        });
        checkpanic self.scheduler.attach(eventInjectorService, self);
        checkpanic self.scheduler.start();
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any parameter0 = parameters[0];
            if (parameter0 is int) {
                self.timeInMilliSeconds = parameter0;
            } else {
                error err = error("TimeBatch window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("TimeBatch window should only have one parameter (<int> " +
                "windowBatchTime), but found " + parameters.length().toString() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {

        lock {
            foreach var evt in streamEvents {
                StreamEvent event = <StreamEvent>evt;
                if (event.eventType == "CURRENT") {
                    StreamEvent clonedEvent = event.copy();
                    self.currentEventQueue.addLast(clonedEvent);
                } if (event.eventType == "TIMER") {
                    self.resetEvent = createResetStreamEvent(event);
                } else {
                    continue;
                }
            }

            LinkedList outputStreamEvents = new();
            boolean sendEvents = !(self.resetEvent is ());
            if (sendEvents) {
                outputStreamEvents.addLast(self.resetEvent);
                if (!(self.currentEventQueue.getFirst() is ())) {
                    self.currentEventQueue.resetToFront();
                    while (self.currentEventQueue.hasNext()) {
                        StreamEvent streamEvent = getStreamEvent(self.currentEventQueue.next());
                        outputStreamEvents.addLast(streamEvent);
                    }
                }
                self.resetEvent = ();
                self.currentEventQueue.clear();
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
            "timeInMilliSeconds": self.timeInMilliSeconds
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
        any millis = state["timeInMilliSeconds"];
        if (millis is int) {
            self.timeInMilliSeconds = millis;
        }
    }
};

# `eventInjectorService` triggers the timer event generation at the given timestamp.
service eventInjectorService = service {
    resource function onTrigger(@tainted TimeBatchWindow timeBatchWindow) {
        map<anydata> data = {};
        int currentTime = time:currentTime().time;
        StreamEvent timerEvent = new(["timer", data], "TIMER", currentTime);
        StreamEvent?[] timerEventWrapper = [];
        timerEventWrapper[0] = timerEvent;
        timeBatchWindow.process(timerEventWrapper);
    }
};

# The `timeBatch` function creates a `TimeBatchWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function timeBatch(any[] windowParameters, public function (StreamEvent?[])? nextProcessPointer = ())
                                                                                                                                                                                                                                                                                                                                                                                                                  returns Window {
    TimeBatchWindow timeBatchWindow = new(nextProcessPointer, windowParameters);
    return timeBatchWindow;
}
