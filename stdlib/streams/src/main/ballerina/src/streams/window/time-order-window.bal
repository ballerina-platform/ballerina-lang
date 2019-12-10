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

# The `TimeOrderWindow` sorts the events to be expired by the given timestamp field by comparing that timestamp value
# to engine system time.
# E.g.
#       from inputStream window `timeOrder(inputStream.timestamp, 4000, true)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# `timeOrder` window should only have three parameters (timestamp field, <int> windowTime, <boolean> dropOlderEvents)
#
# + timeInMillis - description
# + windowParameters - description
# + expiredEventQueue - description
# + nextProcessPointer - description
# + timestamp - description
# + lastTimestamp - description
# + dropOlderEvents - description
# + mergeSort - description
# + scheduler - description
public type TimeOrderWindow object {
    *Window;
    *Snapshotable;
    public int timeInMillis;
    public any[] windowParameters;
    public LinkedList expiredEventQueue;
    public function (StreamEvent?[])? nextProcessPointer;
    public string timestamp;
    public int lastTimestamp;
    public boolean dropOlderEvents;
    public MergeSort mergeSort;
    public Scheduler? scheduler = ();

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.timeInMillis = 0;
        self.lastTimestamp = 0;
        self.timestamp = "";
        self.dropOlderEvents = false;
        self.expiredEventQueue = new;
        self.mergeSort = new([], []);
        self.initParameters(windowParameters);
        self.scheduler = new(function (StreamEvent?[] events) {
                self.process(events);
            });
    }

    public function getScheduler() returns Scheduler {
        return <Scheduler> self.scheduler;
    }

    public function initParameters(any[] parameters) {
        if (parameters.length() == 3) {
            any timestampParam = parameters[0];
            if (timestampParam is string) {
                self.timestamp = timestampParam;
            } else {
                error err = error("TimeOrder window's first parameter, timestamp should be of type string");
                panic err;
            }

            any timeInMillisParam = parameters[1];
            if (timeInMillisParam is int) {
                self.timeInMillis = timeInMillisParam;
            } else {
                error err = error("TimeOrder window's second parameter, windowTime should be of type int");
                panic err;
            }

            any dropOlderEventsParam = parameters[2];
            if (dropOlderEventsParam is boolean) {
                self.dropOlderEvents = dropOlderEventsParam;
            } else {
                error err = error("TimeOrder window's third parameter, dropOlderEvents should be of type boolean");
                panic err;
            }
        } else {
            error err = error("TimeOrder window should only have three parameters (<string> timestamp, <int> " +
                "windowTime, <boolean> dropOlderEvents), but found " + parameters.length().toString() +
                " input attributes");
            panic err;
        }

        (function (map<anydata>) returns anydata)?[] fieldFuncs = [function (map<anydata> x) returns anydata {
            return x[self.timestamp];
        }];
        string[] sortTypes = [ASCENDING];
        self.mergeSort = new(fieldFuncs, sortTypes);
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        lock {
            foreach var event in streamEvents {
                streamEventChunk.addLast(event);
            }

            streamEventChunk.resetToFront();

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = getStreamEvent(streamEventChunk.next());
                int currentTime = time:currentTime().time;
                self.expiredEventQueue.resetToFront();

                while (self.expiredEventQueue.hasNext()) {
                    StreamEvent expiredEvent = getStreamEvent(self.expiredEventQueue.next());
                    int timeDiff = (expiredEvent.timestamp - currentTime) + self.timeInMillis;
                    if (timeDiff <= 0) {
                        self.expiredEventQueue.removeCurrent();
                        streamEventChunk.insertBeforeCurrent(expiredEvent);
                    } else {
                        break;
                    }
                }

                if (streamEvent.eventType == CURRENT) {
                    if (self.dropOlderEvents && (currentTime - self.getTimestamp(streamEvent.data[self.timestamp])) >
                        self.timeInMillis) {
                        streamEventChunk.removeCurrent();
                    } else {
                        StreamEvent clonedEvent = streamEvent.copy();
                        clonedEvent.eventType = EXPIRED;
                        clonedEvent.timestamp = self.getTimestamp(clonedEvent.data[self.timestamp]);
                        self.expiredEventQueue.addLast(clonedEvent);
                        StreamEvent?[] events = [];
                        foreach var e in self.expiredEventQueue.asArray() {
                            if (e is StreamEvent) {
                                events[events.length()] = e;
                            }
                        }
                        self.mergeSort.topDownMergeSort(events);
                        self.expiredEventQueue.clear();
                        foreach var event in events {
                            self.expiredEventQueue.addLast(event);
                        }
                    }

                    if (self.lastTimestamp < self.getTimestamp(streamEvent.data[self.timestamp])) {
                        self.getScheduler().notifyAt(self.getTimestamp(streamEvent.data[self.timestamp]) + self
                                                         .timeInMillis);
                        self.lastTimestamp = self.getTimestamp(streamEvent.data[self.timestamp]);
                    }
                } else {
                    streamEventChunk.removeCurrent();
                }
            }
            self.expiredEventQueue.resetToFront();
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
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
                        public boolean isLHSTrigger = true)
                        returns @tainted [StreamEvent?, StreamEvent?][] {
        [StreamEvent?, StreamEvent?][] events = [];
        int i = 0;
        foreach var e in self.expiredEventQueue.asArray() {
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

    public function getTimestamp(any val) returns (int) {
        if (val is int) {
            return val;
        } else {
            error err = error("timestamp should be of type int");
            panic err;
        }
    }

    # Return current state to be saved as a map of `any` typed values.
    # + return - A map of `any` typed values.
    public function saveState() returns map<any> {
        SnapshottableStreamEvent?[] expiredEventsList = toSnapshottableEvents(self.expiredEventQueue.asArray());
        return {
            "expiredEventsList": expiredEventsList,
            "lastTimestamp": self.lastTimestamp
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var expiredEventsList = state["expiredEventsList"];
        if (expiredEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] expiredEvents = toStreamEvents(expiredEventsList);
            self.expiredEventQueue = new;
            self.expiredEventQueue.addAll(expiredEvents);
        }
        any lts = state["lastTimestamp"];
        if (lts is int) {
            self.lastTimestamp = lts;
        }
    }
};

# The `timeOrder` function creates a `TimeOrderWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function timeOrder(any[] windowParameters, public function (StreamEvent?[])? nextProcessPointer = ())
                                                                                                                                                                                                                                                                                                                                                                                                                  returns Window {

    TimeOrderWindow timeOrderWindow = new(nextProcessPointer, windowParameters);
    return timeOrderWindow;
}
