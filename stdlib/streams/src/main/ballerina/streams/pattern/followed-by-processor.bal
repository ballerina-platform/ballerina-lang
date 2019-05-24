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

public type FollowedByProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public AbstractPatternProcessor? lhsProcessor;
    public AbstractPatternProcessor? rhsProcessor;
    public map<StreamEvent> partialStates;
    public string lhsAlias = "lhs";
    public string rhsAlias = "rhs";

    public function __init() {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.lhsProcessor = ();
        self.rhsProcessor = ();
        self.partialStates = {};
        self.stateMachine = ();
        self.lockField = 0;
    }

    public function process(StreamEvent event, string? processorAlias) returns (boolean, boolean) {
        lock {
            self.lockField += 1;
            boolean promoted = false;
            boolean promote = false;
            boolean tmpPromote = false;
            boolean toNext = false;
            boolean tmpToNext = false;
            // leftward traversal
            AbstractPatternProcessor? lProcessor = self.lhsProcessor;
            if (lProcessor is AbstractPatternProcessor) {
                (tmpPromote, tmpToNext) = lProcessor.process(event, self.lhsAlias);
                promote = promote || tmpPromote;
                toNext = toNext || tmpToNext;
            }
            // rightward traversal
            if ((!promote || toNext) && self.partialStates.length() > 0) {
                toNext = false;
                AbstractPatternProcessor? rProcessor = self.rhsProcessor;
                if (rProcessor is AbstractPatternProcessor) {
                    // foreach partial state, copy event data and process in rhsProcessor.
                    string[] evtIds = self.partialStates.keys().clone();
                    foreach string id in evtIds {
                        StreamEvent partialEvt = <StreamEvent>self.partialStates[id];
                        partialEvt.addData(event.cloneData());
                        // at the leaf nodes (operand processor), it'll take current events'
                        // stream name into consideration. Therefore, we have to set that.
                        partialEvt.streamName = event.streamName;
                        (tmpPromote, tmpToNext) = rProcessor.process(partialEvt, self.rhsAlias);
                        promote = promote || tmpPromote;
                        toNext = toNext || tmpToNext;
                    }
                }
            }
            // upward traversal / promote
            if (promote) {
                AbstractOperatorProcessor? pProcessor = self.prevProcessor;
                if (pProcessor is AbstractOperatorProcessor) {
                    self.stateEvents.resetToFront();
                    while (self.stateEvents.hasNext()) {
                        StreamEvent s = getStreamEvent(self.stateEvents.next());
                        self.stateEvents.removeCurrent();
                        pProcessor.promote(s, processorAlias);
                        promoted = true;
                    }
                }
            }
            return (promoted, toNext);
        }
    }

    public function setStateMachine(StateMachine stateMachine) {
        self.stateMachine = stateMachine;
        stateMachine.register(self);
        AbstractPatternProcessor? lProcessor = self.lhsProcessor;
        if (lProcessor is AbstractPatternProcessor) {
            lProcessor.setStateMachine(stateMachine);
        }
        AbstractPatternProcessor? rProcessor = self.rhsProcessor;
        if (rProcessor is AbstractPatternProcessor) {
            rProcessor.setStateMachine(stateMachine);
        }
    }

    public function validate() {
        AbstractPatternProcessor? lProcessor = self.lhsProcessor;
        if (lProcessor is AbstractPatternProcessor) {
            lProcessor.validate();
        } else {
            panic error("FollowedBy clause must have a left hand side expression.");
        }
        AbstractPatternProcessor? rProcessor = self.rhsProcessor;
        if (rProcessor is AbstractPatternProcessor) {
            rProcessor.validate();
        } else {
            panic error("FollowedBy clause must have a right hand side expression.");
        }
    }

    public function promote(StreamEvent stateEvent, string? processorAlias) {
        string pAlias = <string>processorAlias;
        if (pAlias == self.lhsAlias) {
            // promoted from lhs means, it's a partial state.
            self.partialStates[stateEvent.getEventId()] = stateEvent;
        } else {
            // promoted from rhs means, it's a complete state.
            // so, remove the its respective partial event.
            boolean removed = self.partialStates.remove(stateEvent.getEventId());
            if (removed) {
                self.stateEvents.addLast(stateEvent);
            }
        }
    }

    public function evict(StreamEvent stateEvent, string? processorAlias) {
        // remove matching partial states from this processor.
        self.remove(stateEvent);
        // remove matching states from prev processor.
        AbstractOperatorProcessor? pProcessor = self.prevProcessor;
        if (pProcessor is AbstractOperatorProcessor) {
            pProcessor.evict(stateEvent, processorAlias);
        }
    }

    public function remove(StreamEvent streamEvent) {
        // remove matching partial states from this processor.
        boolean removed = self.partialStates.remove(streamEvent.getEventId());
        // remove matching fulfilled states from this processor.
        self.stateEvents.resetToFront();
        while (self.stateEvents.hasNext()) {
            StreamEvent s = getStreamEvent(self.stateEvents.next());
            if (streamEvent.getEventId() == s.getEventId()) {
                self.stateEvents.removeCurrent();
            }
        }
    }

    public function setPreviousProcessor(AbstractOperatorProcessor processor) {
        self.prevProcessor = processor;
    }

    public function setLHSProcessor(AbstractPatternProcessor processor) {
        self.lhsProcessor = processor;
        self.lhsProcessor.setPreviousProcessor(self);
    }

    public function setRHSProcessor(AbstractPatternProcessor processor) {
        self.rhsProcessor = processor;
        self.rhsProcessor.setPreviousProcessor(self);
    }

    public function getAlias() returns string {
        string alias = "";
        AbstractPatternProcessor? lProcessor = self.lhsProcessor;
        if (lProcessor is AbstractPatternProcessor) {
            alias = alias + lProcessor.getAlias();
        }
        AbstractPatternProcessor? rProcessor = self.rhsProcessor;
        if (rProcessor is AbstractPatternProcessor) {
            alias = alias + " => " + rProcessor.getAlias();
        }
        return alias;
    }
};

public function createFollowedByProcessor() returns FollowedByProcessor {
    FollowedByProcessor followedByProcessor = new;
    return followedByProcessor;
}