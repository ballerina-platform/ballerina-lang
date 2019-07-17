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

# The `LengthWindow` is a sliding length window, that holds last windowLength events, and gets updated on every event
# arrival and expiry.
# E.g.
#       from inputStream window `length(5)`
#       select inputStream.name, inputStream.age, sum(inputStream.age) as sumAge, count() as count
#       group by inputStream.name => (TeacherOutput [] teachers) {
#            foreach var t in teachers {
#                outputStream.publish(t);
#            }
#        }
# The `length` window should only have one parameter (<int> windowLength)
#
# + size - description
# + linkedList - description
# + windowParameters - description
# + nextProcessPointer - description
public type LengthWindow object {
    *Window;
    *Snapshotable;
    public int size;
    public LinkedList linkedList;
    public any[] windowParameters;
    public function (StreamEvent?[])? nextProcessPointer;

    public function __init(function (StreamEvent?[])? nextProcessPointer, any[] windowParameters) {
        self.nextProcessPointer = nextProcessPointer;
        self.windowParameters = windowParameters;
        self.linkedList = new;
        self.size = 0;
        self.initParameters(windowParameters);
    }


    public function initParameters(any[] parameters) {
        if (parameters.length() == 1) {
            any value = parameters[0];
            if (value is int) {
                self.size = value;
            } else {
                error err = error("Length window expects an int parameter");
                panic err;
            }
        } else {
            error err = error("Length window should only have one parameter (<int> " +
                "windowLength), but found " + parameters.length().toString() + " input attributes");
            panic err;
        }
    }

    # The `process` function process the incoming events to the events and update the current state of the window.
    # + streamEvents - The array of stream events to be processed.
    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] outputEvents = [];
        foreach var evt in streamEvents {
            StreamEvent event = <StreamEvent>evt;
            if (self.linkedList.getSize() == self.size) {

                any anyValue = self.linkedList.removeFirst();
                if (anyValue is StreamEvent) {
                    outputEvents[outputEvents.length()] = anyValue;
                }
            }

            outputEvents[outputEvents.length()] = event;
            StreamEvent expiredVeresionOfEvent = event.copy();
            expiredVeresionOfEvent.eventType = "EXPIRED";
            self.linkedList.addLast(expiredVeresionOfEvent);
        }

        any nextProcessFuncPointer = self.nextProcessPointer;
        if (nextProcessFuncPointer is function (StreamEvent?[])) {
            nextProcessFuncPointer(outputEvents);
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
        foreach var e in self.linkedList.asArray() {
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
        SnapshottableStreamEvent?[] eventsList = toSnapshottableEvents(self.linkedList.asArray());
        return {
            "eventsList": eventsList
        };
    }

    # Restores the saved state which is passed as a map of `any` typed values.
    # + state - A map of typed `any` values. This map contains the values to be restored from the persisted data.
    public function restoreState(map<any> state) {
        var eventsList = state["eventsList"];
        if (eventsList is SnapshottableStreamEvent?[]) {
            StreamEvent?[] streamEvents = toStreamEvents(eventsList);
            self.linkedList = new;
            self.linkedList.addAll(streamEvents);
        }
    }
};

# The `length` function creates a `LengthWindow` object and returns it.
# + windowParameters - Arguments which should be passed with the window function in the streams query in the order
#                       they appear in the argument list.
# + nextProcessPointer - The function pointer to the `process` function of the next processor.
# + return - Returns the created window.
public function length(any[] windowParameters, public function (StreamEvent?[])? nextProcessPointer = ())
                                                                                                                                                                                                                                                                                                                                                                                                            returns Window {
    LengthWindow lengthWindow1 = new(nextProcessPointer, windowParameters);
    return lengthWindow1;
}
