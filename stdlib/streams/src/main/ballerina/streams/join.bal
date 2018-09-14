import ballerina/io;

public type JoinProcesor object {
    private function (StreamEvent e1, StreamEvent e2) returns boolean onConditionFunc;
    private function (any) nextProcessor;
    public LengthWindow? lhsWindow;
    public LengthWindow? rhsWindow;
    public string? lhsStream;
    public string? rhsStream;
    public string? unidirectionalStream;
    public JoinType joinType;

    public new(nextProcessor, joinType, onConditionFunc) {
        lhsWindow = ();
        rhsWindow = ();
        lhsStream = ();
        rhsStream = ();
        unidirectionalStream = ();
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent?[] joinedEvents;
        int i = 0;
        foreach event in streamEvents {
            string originStream = getStreamName(event);
            // resolve trigger according to join direction
            boolean triggerJoin = false;
            match unidirectionalStream {
                string s => {
                    // unidirectional
                    if (s.equalsIgnoreCase(originStream)) {
                        triggerJoin = true;
                    }
                }
                () => {
                    // bidirectional
                    triggerJoin = true;
                }
            }

            if (triggerJoin) {
                (StreamEvent?, StreamEvent?)[] candidateEvents;
                // join events according to the triggered side
                if (self.lhsStream.equalsIgnoreCase(originStream) ?: false) {
                    // triggered from LHS
                    match rhsWindow.getCandidateEvents(event, onConditionFunc) {
                        (StreamEvent, StreamEvent)[] evtArr => {
                            candidateEvents = evtArr;
                            // with left/full joins, we need to emit an event even there's no candidate events in rhs.
                            if (lengthof candidateEvents == 0 && (joinType == "LEFT" || joinType == "FULL")) {
                                candidateEvents[0] = (event, ());
                            }
                        }
                        () => {
                            if (joinType == "LEFT" || joinType == "FULL") {
                                candidateEvents[0] = (event, ());
                            }
                        }
                    }
                    foreach evtTuple in candidateEvents {
                        joinedEvents[i] = joinEvents(evtTuple[0], evtTuple[1]);
                        i++;
                    }
                } else {
                    match lhsWindow.getCandidateEvents(event, onConditionFunc, isLHSTrigger = false) {
                        (StreamEvent, StreamEvent)[] evtArr => {
                            candidateEvents = evtArr;
                            // with right/full joins, we need to emit an event even there's no candidate events in rhs.
                            if (lengthof candidateEvents == 0 && (joinType == "RIGHT" || joinType == "FULL")) {
                                candidateEvents[0] = ((), event);
                            }
                        }
                        () => {
                            if (joinType == "RIGHT" || joinType == "FULL") {
                                candidateEvents[0] = ((), event);
                            }
                        }
                    }
                    foreach evtTuple in candidateEvents {
                        joinedEvents[i] = joinEvents(evtTuple[0], evtTuple[1], lhsTriggered = false);
                        i++;
                    }
                }
            }
        }

        StreamEvent[] outputEvents;
        i = 0;
        foreach e in joinedEvents {
            match e {
                StreamEvent s => {
                    outputEvents[i] = s;
                    i++;
                }
                () => {
                }
            }
        }
        nextProcessor(outputEvents);
    }

    public function setLHS(string streamName, LengthWindow windowInstance) {
        self.lhsStream = streamName;
        self.lhsWindow = windowInstance;
    }

    public function setRHS(string streamName, LengthWindow windowInstance) {
        self.rhsStream = streamName;
        self.rhsWindow = windowInstance;
    }

    public function setUnidirectionalStream(string streamName) {
        self.unidirectionalStream = streamName;
    }

    function getStreamName(StreamEvent e) returns string {
        return e.data.keys()[0].split("\\.")[0];
    }

    function joinEvents(StreamEvent? lhsEvent, StreamEvent? rhsEvent, boolean lhsTriggered = true)
                 returns StreamEvent? {
        StreamEvent? joined = ();
        if (joinType == "LEFT") {
            // Left outer join: Returns all the events of left stream
            // even if there are no matching events in the right stream.
            match lhsEvent {
                StreamEvent lhs => {
                    joined = lhs.clone();
                    match rhsEvent {
                        StreamEvent rhs => {
                            joined.addData(rhs.data);
                        }
                        () => {
                            // nothing to do.
                        }
                    }
                }
                () => {
                    // nothing to do.
                }
            }
        } else if (joinType == "RIGHT") {
            // Right outer join: Returns all the events of the right stream
            // even if there are no matching events in the left stream.
            match rhsEvent {
                StreamEvent rhs => {
                    joined = rhs.clone();
                    match lhsEvent {
                        StreamEvent lhs => {
                            joined.addData(lhs.data);
                        }
                        () => {
                            // nothing to do.
                        }
                    }
                }
                () => {
                    // nothing to do.
                }
            }
        } else if (joinType == "FULL") {
            // Full outer join: output event are generated for each incoming
            // event even if there are no matching events in the other stream.
            if (lhsTriggered) {
                match lhsEvent {
                    StreamEvent lhs => {
                        joined = lhs.clone();
                        match rhsEvent {
                            StreamEvent rhs => {
                                joined.addData(rhs.data);
                            }
                            () => {
                                // nothing to do.
                            }
                        }
                    }
                    () => {
                        // nothing to do.
                    }
                }
            } else {
                match rhsEvent {
                    StreamEvent rhs => {
                        joined = rhs.clone();
                        match lhsEvent {
                            StreamEvent lhs => {
                                joined.addData(lhs.data);
                            }
                            () => {
                                // nothing to do.
                            }
                        }
                    }
                    () => {
                        // nothing to do.
                    }
                }
            }
        } else {
            // Inner join (join): The output is generated only if
            // there is a matching event in both the streams.
            StreamEvent lEvt = lhsEvent but {
                () => new StreamEvent({}, "CURRENT", 1)
            };
            StreamEvent rEvt = rhsEvent but {
                () => new StreamEvent({}, "CURRENT", 1)
            };
            if (lhsTriggered) {
                joined = lEvt.clone();
                joined.addData(rEvt.data);
            } else {
                joined = rEvt.clone();
                joined.addData(lEvt.data);
            }
        }
        return joined;
    }
};

public function createJoinProcesor(function (any) nextProcessor, JoinType joinType,
                                   function (StreamEvent e1, StreamEvent e2)
                                       returns boolean conditionFunc)
                    returns JoinProcesor {
    JoinProcesor joinProcesor = new(nextProcessor, joinType, conditionFunc);
    return joinProcesor;
}