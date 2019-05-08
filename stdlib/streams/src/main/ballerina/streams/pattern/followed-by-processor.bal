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
    }

    public function process(StreamEvent event, string? processorAlias) returns (boolean, boolean) {
        io:println("FollowedByProcessor:process:38 -> ", event, "|", processorAlias);
        boolean promoted = false;
        boolean promote = false;
        boolean tmpPromote = false;
        boolean toNext = false;
        boolean tmpToNext = false;
        // leftward traversal
        AbstractPatternProcessor? lProcessor = self.lhsProcessor;
        if (lProcessor is AbstractPatternProcessor) {
            io:println("FollowedByProcessor:process:47 -> ", event, "|", processorAlias);
            (tmpPromote, tmpToNext) = lProcessor.process(event, self.lhsAlias);
            promote = promote || tmpPromote;
            toNext = toNext || tmpToNext;
            io:println("FollowedByProcessor:process:51 -> ", event, "|", processorAlias);
        }
        // rightward traversal
        if ((!promote || toNext) && self.partialStates.length() > 0) {
            toNext = false;
            AbstractPatternProcessor? rProcessor = self.rhsProcessor;
            if (rProcessor is AbstractPatternProcessor) {
                // foreach partial state, copy event data and process in rhsProcessor.
                foreach var (id, partialEvt) in self.partialStates {
                    StreamEvent clone = partialEvt.copy();
                    clone.addData(event.cloneData());
                    // at the leaf nodes (operand processor), it'll take current events'
                    // stream name into consideration. Therefore, we have to set that.
                    clone.streamName = event.streamName;
                    io:println("FollowedByProcessor:process:65 -> ", clone, "|", processorAlias);
                    (tmpPromote, tmpToNext) = rProcessor.process(clone, self.rhsAlias);
                    promote = promote || tmpPromote;
                    toNext = toNext || tmpToNext;
                    io:println("FollowedByProcessor:process:69 -> ", clone, "|", processorAlias);
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
                    io:println("FollowedByProcessor:process:81 -> ", s, "|", processorAlias);
                    pProcessor.promote(s, processorAlias);
                    io:println("FollowedByProcessor:process:83 -> ", s, "|", processorAlias);
                    promoted = true;
                }
            }
        }
        io:println("FollowedByProcessor:process:88 -> ", event, "|", processorAlias);
        return (promoted, toNext);
    }

    public function setStateMachine(StateMachine stateMachine) {
        self.stateMachine = stateMachine;
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
            io:println("FollowedByProcessor:promote:122 -> ", stateEvent, "|", processorAlias);
            // promoted from lhs means, it's a partial state.
            self.partialStates[stateEvent.getEventId()] = stateEvent;
        } else {
            // promoted from rhs means, it's a complete state.
            // so, remove the its respective partial event.
            io:println("FollowedByProcessor:promote:128 -> ", stateEvent, "|", processorAlias);
            boolean removed = self.partialStates.remove(stateEvent.getEventId());
            if (removed) {
                io:println("FollowedByProcessor:promote:131 -> ", stateEvent, "|", processorAlias);
                self.stateEvents.addLast(stateEvent);
            }
        }
    }

    public function evict(StreamEvent stateEvent, string? processorAlias) {
        // remove matching partial states from this processor.
        boolean removed = self.partialStates.remove(stateEvent.getEventId());
        // remove matching fulfilled states from this processor.
        self.stateEvents.resetToFront();
        while (self.stateEvents.hasNext()) {
            StreamEvent s = getStreamEvent(self.stateEvents.next());
            if (stateEvent.getEventId() == s.getEventId()) {
                self.stateEvents.removeCurrent();
            }
        }
        // remove matching states from prev processor.
        AbstractOperatorProcessor? pProcessor = self.prevProcessor;
        if (pProcessor is AbstractOperatorProcessor) {
            io:println("FollowedByProcessor:evict:151 -> ", stateEvent, "|", processorAlias);
            pProcessor.evict(stateEvent, processorAlias);
            io:println("FollowedByProcessor:evict:153 -> ", stateEvent, "|", processorAlias);
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