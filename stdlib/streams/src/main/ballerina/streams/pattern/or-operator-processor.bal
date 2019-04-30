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

public type OrOperatorProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public AbstractPatternProcessor? lhsProcessor;
    public AbstractPatternProcessor? rhsProcessor;
    public string lhsAlias = "lhs";
    public string rhsAlias = "rhs";

    public function __init() {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.lhsProcessor = ();
        self.rhsProcessor = ();
        self.stateMachine = ();
    }

    public function process(StreamEvent event, string? processorAlias) returns boolean {
        io:println("OrOperatorProcessor:process:35 -> ", event, "|", processorAlias);
        boolean promote = false;
        boolean promoted = false;
        // leftward traversal
        AbstractPatternProcessor? lProcessor = self.lhsProcessor;
        if (lProcessor is AbstractPatternProcessor) {
            io:println("OrOperatorProcessor:process:41 -> ", event, "|", processorAlias);
            promote = lProcessor.process(event, self.lhsAlias);
            io:println("OrOperatorProcessor:process:43 -> ", event, "|", processorAlias);
        }
        // rightward traversal
        AbstractPatternProcessor? rProcessor = self.rhsProcessor;
        if (!promote && rProcessor is AbstractPatternProcessor) {
            io:println("OrOperatorProcessor:process:48 -> ", event, "|", processorAlias);
            promote = rProcessor.process(event, self.rhsAlias);
            io:println("OrOperatorProcessor:process:50 -> ", event, "|", processorAlias);
        }
        // upward traversal / promote
        if (promote) {
            AbstractOperatorProcessor? pProcessor = self.prevProcessor;
            if (pProcessor is AbstractOperatorProcessor) {
                self.stateEvents.resetToFront();
                while (self.stateEvents.hasNext()) {
                    StreamEvent s = getStreamEvent(self.stateEvents.next());
                    self.stateEvents.removeCurrent();
                    io:println("OrOperatorProcessor:process:60 -> ", s, "|", processorAlias);
                    pProcessor.promote(s, processorAlias);
                    io:println("OrOperatorProcessor:process:62 -> ", s, "|", processorAlias);
                    promoted = true;
                }
            }
        }
        return promoted;
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

    public function promote(StreamEvent stateEvent, string? processorAlias) {
        io:println("OrOperatorProcessor:promote:71 -> ", stateEvent, "|", processorAlias);
        self.stateEvents.addLast(stateEvent);
    }

    public function evict(StreamEvent stateEvent, string? processorAlias) {
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
            io:println("OrOperatorProcessor:evict:87 -> ", stateEvent, "|", processorAlias);
            pProcessor.evict(stateEvent, processorAlias);
            io:println("OrOperatorProcessor:evict:89 -> ", stateEvent, "|", processorAlias);
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
            alias += " || " + rProcessor.getAlias();
        } else {
            alias += " || " + self.rhsAlias;
        }
        return alias;
    }
};

public function createOrOperatorProcessor() returns OrOperatorProcessor {
    OrOperatorProcessor orOperatorProcessor = new;
    return orOperatorProcessor;
}
