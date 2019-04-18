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

import ballerina/io;

public type AndOperatorProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public AbstractPatternProcessor? lhsProcessor;
    public AbstractPatternProcessor? rhsProcessor;
    public map<StreamEvent> lhsPartialStates;
    public map<StreamEvent> rhsPartialStates;
    public string lhsAlias = "lhs";
    public string rhsAlias = "rhs";

    public function __init() {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.lhsProcessor = ();
        self.rhsProcessor = ();
        self.lhsPartialStates = {};
        self.rhsPartialStates = {};
    }

    public function process(StreamEvent event, string? processorAlias) returns boolean {
        boolean promote = false;
        boolean promoted = false;
        // leftward traversal
        AbstractPatternProcessor? lProcessor = self.lhsProcessor;
        if (lProcessor is AbstractPatternProcessor) {
            if (self.rhsPartialStates.length() > 0) {
                // foreach rhs partial state, copy event data and process in lhsProcessor.
                foreach var (id, partialEvt) in self.rhsPartialStates {
                    StreamEvent clone = partialEvt.copy();
                    clone.addData(event.cloneData());
                    promote = promote || lProcessor.process(clone, self.lhsAlias);
                }
            } else {
                promote = lProcessor.process(event, self.lhsAlias);
            }
        }
        // rightward traversal
        if (!promote) {
            AbstractPatternProcessor? rProcessor = self.rhsProcessor;
            if (rProcessor is AbstractPatternProcessor) {
                if (self.lhsPartialStates.length() > 0) {
                    // foreach lhs partial state, copy event data and process in rhsProcessor.
                    foreach var (id, partialEvt) in self.lhsPartialStates {
                        StreamEvent clone = partialEvt.copy();
                        clone.addData(event.cloneData());
                        promote = promote || rProcessor.process(clone, self.rhsAlias);
                    }
                } else {
                    promote = rProcessor.process(event, self.rhsAlias);
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
        return promoted;
    }

    public function promote(StreamEvent stateEvent, string? processorAlias) {
        string pAlias = <string>processorAlias;
        if (pAlias == self.lhsAlias) {
            // promoted from lhs means, it can be a partial lhs state or a completed state.
            boolean rhsRemoved = self.rhsPartialStates.remove(stateEvent.getEventId());
            // rhsRemoved=true means, it was earlier a partial rhs state, and now it's a completed state.
            if (rhsRemoved) {
                self.stateEvents.addLast(stateEvent);
            } else {
                self.lhsPartialStates[stateEvent.getEventId()] = stateEvent;
            }
        } else {
            // promoted from rhs means, it can be a partial rhs state or a completed state.
            boolean lhsRemoved = self.lhsPartialStates.remove(stateEvent.getEventId());
            // lhsRemoved=true means, it was earlier a partial lhs state, and now it's a completed state.
            if (lhsRemoved) {
                self.stateEvents.addLast(stateEvent);
            } else {
                self.rhsPartialStates[stateEvent.getEventId()] = stateEvent;
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
        } else {
            alias += self.lhsAlias;
        }
        AbstractPatternProcessor? rProcessor = self.rhsProcessor;
        if (rProcessor is AbstractPatternProcessor) {
            alias += " && " + rProcessor.getAlias();
        } else {
            alias += " && " + self.rhsAlias;
        }
        return alias;
    }
};

public function createAndOperatorProcessor() returns AndOperatorProcessor {
    AndOperatorProcessor andOperatorProcessor = new;
    return andOperatorProcessor;
}
