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

# The sort window hold a given number of events and emit the expired events in the ordered by the given fields.
# E.g.
#       from inputStream window `sort(10, inputStream.age, "ascending", inputStream.name, "descending")`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `sort` window should have three or more odd no of parameters (<int> windowLength, stream field, <string>
# order1,  stream field, <string> order2, ...)
#
# + lengthToKeep - description
# + windowParameters - description
# + sortedWindow - description
# + fields - description
# + sortTypes - description
# + nextProcessPointer - description
# + fieldFuncs - description
# + mergeSort - description
public type SortWindow object {
    *Window;
    *Snapshotable;
    public int lengthToKeep;
    public any [] windowParameters;
    public LinkedList sortedWindow;
    public string[] fields;
    public string[] sortTypes;
    public function (StreamEvent?[])? nextProcessPointer;
    public (function (map<anydata>) returns anydata)?[] fieldFuncs;
    public MergeSort mergeSort;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.sortedWindow = new;
        self.lengthToKeep = 0;
        self.fields = [];
        self.sortTypes = [];
        self.fieldFuncs = [];
        self.mergeSort = new(self.fieldFuncs, self.sortTypes);
        self.initParameters(windowParameters);
    }

    public function initParameters(any[] parameters) {
        if(!(parameters.length() >= 3 && parameters.length() % 2 == 1)) {
            error err = error("Sort window should have three or more odd no of" +
                "parameters (<int> windowLength, <string> attribute1, <string> order1, " +
                "<string> attribute2, <string> order2, ...), but found " + parameters.length().toString()
                + " input attributes" );
            panic err;
        }

        any parameter0 = parameters[0];
        if(parameter0 is int) {
            self.lengthToKeep = parameter0;
        } else {
            error err = error("Sort window's first parameter, windowLength should be of type int");
            panic err;
        }

        int i = 1;
        while(i < parameters.length()) {
            any nextParameter = parameters[i];
            int intParameter;
            if(nextParameter is string) {
                if (i % 2 == 1) {
                    self.fields[self.fields.length()] = nextParameter;
                } else {
                    if (nextParameter == ASCENDING || nextParameter == DESCENDING) {
                        self.sortTypes[self.sortTypes.length()] = nextParameter;
                    } else {
                        intParameter = i + 1;
                        error err = error("Expected ascending or descending at parameter " + intParameter.toString() +
                            " of sort window");
                        panic err;
                    }
                }
            } else if(nextParameter is int) {
                intParameter = i + 1;
                error err = error("Expected string parameter at parameter " + intParameter.toString() +
                    " of sort window, but found <int>");
                panic err;
            } else if(nextParameter is float) {
                intParameter = i + 1;
                error err = error("Expected string parameter at parameter " + intParameter.toString() +
                    " of sort window, but found <float>");
                panic err;
            } else if(nextParameter is boolean) {
                intParameter = i + 1;
                error err = error("Expected string parameter at parameter " + intParameter.toString() +
                    " of sort window, but found <boolean>");
                panic err;
            } else {
                error err = error("Incompatible parameter type" );
                panic err;
            }
            i += 1;
        }

        foreach string field in self.fields {
            self.fieldFuncs[self.fieldFuncs.length()] = function (map<anydata> x) returns anydata {
                return x[field];
            };
        }

        self.mergeSort = new(self.fieldFuncs, self.sortTypes);
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    #
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        LinkedList streamEventChunk = new;
        foreach var event in streamEvents {
            streamEventChunk.addLast(event);
        }

        if (streamEventChunk.getFirst() is ()) {
            return;
        }

        lock {
            int currentTime = time:currentTime().time;

            while (streamEventChunk.hasNext()) {
                StreamEvent streamEvent = <StreamEvent>streamEventChunk.next();

                StreamEvent clonedEvent = streamEvent.copy();
                clonedEvent.eventType = EXPIRED;

                self.sortedWindow.addLast(clonedEvent);
                if (self.sortedWindow.getSize() > self.lengthToKeep) {
                    StreamEvent?[] events = [];
                    self.sortedWindow.resetToFront();

                    while (self.sortedWindow.hasNext()) {
                        StreamEvent streamEven = <StreamEvent>self.sortedWindow.next();
                        events[events.length()] = streamEven;
                    }

                    self.mergeSort.topDownMergeSort(events);
                    self.sortedWindow.clear();
                    foreach var event in events {
                        self.sortedWindow.addLast(event);
                    }

                    StreamEvent expiredEvent = <StreamEvent>self.sortedWindow.removeLast();
                    expiredEvent.timestamp = currentTime;
                    streamEventChunk.insertBeforeCurrent(expiredEvent);
                }
            }
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            if (streamEventChunk.getSize() != 0) {
                StreamEvent?[] events = [];
                streamEventChunk.resetToFront();
                while (streamEventChunk.hasNext()) {
                    StreamEvent streamEvent = <StreamEvent>streamEventChunk.next();
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
        foreach var e in self.sortedWindow.asArray() {
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
        SnapshottableStreamEvent?[] sortEventsList = toSnapshottableEvents(self.sortedWindow.asArray());
        return {
            "sortEventsList": sortEventsList
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var sortEventsList = state["sortEventsList"];
        if (sortEventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] sortedEvents = toStreamEvents(sortEventsList);
            self.sortedWindow = new;
            self.sortedWindow.addAll(sortedEvents);
        }
    }
};

# The `sort` function creates a `SortWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function sort(any[] windowParameters, public function (StreamEvent?[])? nextProcessPointer = ())
                                                                                                                                                                                                                                                                                                                                                                                                        returns Window {
    SortWindow sortWindow1 = new(nextProcessPointer, windowParameters);
    return sortWindow1;
}
