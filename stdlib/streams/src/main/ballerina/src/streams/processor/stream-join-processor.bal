// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# The `StreamJoinProcessor` object is responsible for  performing SQLish joins between two or more streams.
# The `onConditionFunc` is the lambda function which represents the where clause in the join clause. The joining
# happens only if the condition is statified. `nextProcessor` is the `process` function of the next processor, which
# can be a `Select` processor, `Aggregator` processor, `Having` processor.. etc. The `lhsStream` is the left hand side
# stream of the join and its attached window is `'lhsWindow`. The `rhsStream` is the right hand side stream of the join
# and its attached window is `'rhsWindow`. The `unidirectionalStream` stream defines the stream by which the joining is
# triggered when the events are received. Usually it is `lhsStream`, in rare cases it can be `rhsStream`. The
# `joinType` is the type of the join and it can be any value defined by `streams:JoinType`.
#
# + onConditionFunc - description
# + nextProcessor - description
# + lhsWindow - description
# + rhsWindow - description
# + lhsStream - description
# + rhsStream - description
# + unidirectionalStream - description
# + joinType - description
# + lockField - description
public type StreamJoinProcessor object {
    private (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? onConditionFunc;
    private function (StreamEvent?[]) nextProcessor;
    public Window? lhsWindow;
    public Window? rhsWindow;
    public string? lhsStream;
    public string? rhsStream;
    public string? unidirectionalStream;
    public JoinType joinType;
    public int lockField = 0;

    public function __init(function (StreamEvent?[]) nextProcessor, JoinType joinType,
                           (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)? onConditionFunc) {
        self.nextProcessor = nextProcessor;
        self.joinType = joinType;
        self.onConditionFunc = onConditionFunc;
        self.lhsWindow = ();
        self.rhsWindow = ();
        self.lhsStream = ();
        self.rhsStream = ();
        self.unidirectionalStream = ();
    }

    # Process the events from both `lhsStream` and the `rhsStream` and perform the joining.
    # + streamEvents - Stream events being joined.
    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] joinedEvents = [];
        StreamEvent?[] outputEvents = [];
        lock {
            self.lockField += 1;
            int i = 0;
            foreach var evt in streamEvents {
                StreamEvent event = <StreamEvent> evt;
                string[] values = internal:split(event.data.keys()[0], "\\.");
                string originStream = values[0];
                // resolve trigger according to join direction
                boolean triggerJoin = false;
                var s = self.unidirectionalStream;
                if (s is string) {
                    // unidirectional
                    if (internal:equalsIgnoreCase(s, originStream)) {
                        triggerJoin = true;
                    }
                } else {
                    // bidirectional
                    triggerJoin = true;
                }

                if (triggerJoin) {
                    [StreamEvent?, StreamEvent?][] candidateEvents = [];
                    // join events according to the triggered side
                    string? value = self.lhsStream;
                    string returnVal = value is string ? value : "";
                    if (internal:equalsIgnoreCase(returnVal, originStream)) {
                        // triggered from LHS
                        Window? rWindow = self.rhsWindow;
                        if (rWindow is Window) {
                            candidateEvents = rWindow.getCandidateEvents(event, self.onConditionFunc);
                            // with left/full joins, we need to emit an event even there's no candidate events in rhs.
                            if (candidateEvents.length() == 0 && (self.joinType == "LEFTOUTERJOIN"
                                    || self.joinType == "FULLOUTERJOIN")) {
                                candidateEvents[0] = [event, ()];
                            }
                        } else if (self.joinType == "LEFTOUTERJOIN" || self.joinType == "FULLOUTERJOIN") {
                            candidateEvents[0] = [event, ()];
                        }
                        foreach var evtTuple in candidateEvents {
                            joinedEvents[i] = self.joinEvents(evtTuple[0], evtTuple[1]);
                            i += 1;
                        }
                    } else {
                        //var evtArr = self.lhsWindow.getCandidateEvents(event, self.onConditionFunc, isLHSTrigger = false);
                        Window? lWindow = self.lhsWindow;
                        if (lWindow is Window) {
                            var evtArr = lWindow.getCandidateEvents(event, self.onConditionFunc, isLHSTrigger = false);
                            candidateEvents = evtArr;
                            // with right/full joins, we need to emit an event even there's no candidate events in rhs.
                            if (candidateEvents.length() == 0 && (self.joinType == "RIGHTOUTERJOIN"
                                    || self.joinType == "FULLOUTERJOIN")) {
                                candidateEvents[0] = [(), event];
                            }
                        } else if (self.joinType == "RIGHTOUTERJOIN" || self.joinType == "FULLOUTERJOIN") {
                            candidateEvents[0] = [(), event];
                        }
                        foreach var evtTuple in candidateEvents {
                            joinedEvents[i] = self.joinEvents(evtTuple[0], evtTuple[1], lhsTriggered = false);
                            i += 1;
                        }
                    }
                }
            }

            outputEvents = [];
            i = 0;
            foreach var e in joinedEvents {
                if (e is StreamEvent) {
                    outputEvents[i] = e;
                    i += 1;
                }
            }
        }
        function (StreamEvent?[]) nProcessor = self.nextProcessor;
        nProcessor(outputEvents);
    }

    # Sets the left hand side stream name and the respective window instance.
    # + streamName - The name of the left hand side stream.
    # + windowInstance -   The window attached to the left hand side stream.
    public function setLHS(string streamName, Window windowInstance) {
        self.lhsStream = streamName;
        self.lhsWindow = windowInstance;
    }

    # Sets the right hand side stream name and the respective window instance.
    # + streamName - The name of the right hand side stream.
    # + windowInstance -   The window attached to the right hand side stream.
    public function setRHS(string streamName, Window windowInstance) {
        self.rhsStream = streamName;
        self.rhsWindow = windowInstance;
    }

    # Sets the stream by which the joining is triggered.
    # + streamName - The name of the stream. In most cases, the joining is triggered when the events are received by
    #                the left hand side stream even if the right hand side stream receives the events before the left
    #                hand side stream receives events.
    public function setUnidirectionalStream(string streamName) {
        self.unidirectionalStream = streamName;
    }

    function joinEvents(StreamEvent? lhsEvent, StreamEvent? rhsEvent, boolean lhsTriggered = true)
                 returns StreamEvent? {
        StreamEvent? joined = ();
        if (self.joinType == "LEFTOUTERJOIN") {
            // Left outer join: Returns all the events of left stream
            // even if there are no matching events in the right stream.
            if (lhsEvent is StreamEvent) {
                joined = lhsEvent.copy();

                if (rhsEvent is StreamEvent && joined is StreamEvent) {
                    joined.addData(rhsEvent.data);
                } else {
                    // nothing to do.
                }
            } else {
                // nothing to do.
            }
        } else if (self.joinType == "RIGHTOUTERJOIN") {
            // Right outer join: Returns all the events of the right stream
            // even if there are no matching events in the left stream.

            if (rhsEvent is StreamEvent) {
                joined = rhsEvent.copy();

                if (lhsEvent is StreamEvent && joined is StreamEvent) {
                    joined.addData(lhsEvent.data);
                } else {
                    // nothing to do.
                }

            } else {
                // nothing to do.
            }
        } else if (self.joinType == "FULLOUTERJOIN") {
            // Full outer join: output event are generated for each incoming
            // event even if there are no matching events in the other stream.
            if (lhsTriggered) {

                if (lhsEvent is StreamEvent) {
                    joined = lhsEvent.copy();

                    if (rhsEvent is StreamEvent && joined is StreamEvent) {
                        joined.addData(rhsEvent.data);
                    } else {
                        // nothing to do.
                    }
                } else {
                    // nothing to do.
                }
            } else {
                if (rhsEvent is StreamEvent) {
                    joined = rhsEvent.copy();
                    if (lhsEvent is StreamEvent && joined is StreamEvent) {
                        joined.addData(lhsEvent.data);
                    } else {
                        // nothing to do.
                    }
                } else {
                    // nothing to do.
                }
            }
        } else {
            // Inner join (join): The output is generated only if
            // there is a matching event in both the streams.
            StreamEvent lEvt = lhsEvent is ()? new
            StreamEvent({}, "CURRENT", 1) : <
                StreamEvent > lhsEvent;
            StreamEvent rEvt = rhsEvent is ()? new
            StreamEvent({}, "CURRENT", 1) : <
                StreamEvent > rhsEvent;

            if (lhsTriggered) {
                joined = lEvt.copy();
                if (joined is StreamEvent) {
                    joined.addData(rEvt.data);
                }
            } else {
                joined = rEvt.copy();
                if (joined is StreamEvent) {
                    joined.addData(lEvt.data);
                }
            }
        }
        return joined;
    }
};

# Creates a `StreamJoinProcessor` and returns it.
#
# + nextProcessor - The `process` function of the next processor, which can be a `Select` processor, `Aggregator`
#                   processor, `Having` processor.. etc.
# + joinType - Type of the join being performed ("JOIN"|"LEFTOUTERJOIN"|"RIGHTOUTERJOIN"|"FULLOUTERJOIN")
# + conditionFunc - A lambda function which contains the joining condition and return true if the condition satifies
#                   the condition.
#
# + return - Returns a `StreamJoinProcessor` object.
public function createStreamJoinProcessor(function (StreamEvent?[]) nextProcessor, JoinType joinType,
                                          public (function (map<anydata> e1Data, map<anydata> e2Data) returns boolean)?
                                          conditionFunc = ())
                    returns StreamJoinProcessor {
    StreamJoinProcessor joinProcesor = new(nextProcessor, joinType, conditionFunc);
    return joinProcesor;
}
