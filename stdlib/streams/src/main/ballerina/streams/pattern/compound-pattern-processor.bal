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
import ballerina/time;

public type CompoundPatternProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public AbstractPatternProcessor? processor;
    public StreamEvent?[] fulfilledEvents;
    // time from initial state to current state should be within this time
    public int? withinTimeMillis;

    public function __init(int? withinTimeMillis) {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.processor = ();
        self.fulfilledEvents = [];
        self.withinTimeMillis = withinTimeMillis;
    }

    public function process(StreamEvent event, string? processorAlias) returns boolean {
        io:println("CompoundPatternProcessor:process:36 -> ", event, "|", processorAlias);
        boolean promote = false;
        boolean promoted = false;
        // downward traversal
        AbstractPatternProcessor? processor = self.processor;
        if (processor is AbstractPatternProcessor) {
            // processorAlias is not required when get promoted by
            // its only imidiate descendent. Therefore passing ().
            io:println("CompoundPatternProcessor:process:44 -> ", event, "|", processorAlias);
            promote = processor.process(event, ());
            io:println("CompoundPatternProcessor:process:46 -> ", event, "|", processorAlias);
        }
        // upward traversal
        if (promote) {
            int currentTime = time:currentTime().time;
            int? withinTime = self.withinTimeMillis;
            AbstractOperatorProcessor? pProcessor = self.prevProcessor;
            if (pProcessor is AbstractOperatorProcessor) {
                self.stateEvents.resetToFront();
                while (self.stateEvents.hasNext()) {
                    StreamEvent s = getStreamEvent(self.stateEvents.next());
                    self.stateEvents.removeCurrent();
                    if (withinTime is int && (currentTime - s.timestamp) > withinTime) {
                        // state haven't fulfilled in within time, therefore evict.
                        io:println("CompoundPatternProcessor:process:60 -> ", s, "|", processorAlias);
                        pProcessor.evict(s, processorAlias);
                    } else {
                        io:println("CompoundPatternProcessor:process:63 -> ", s, "|", processorAlias);
                        pProcessor.promote(s, processorAlias);
                        io:println("CompoundPatternProcessor:process:65 -> ", s, "|", processorAlias);
                        promoted = true;
                    }
                }
            } else {
                // pProcessor is (). Meaning, this is the root of the
                // pattern topology. So, emit events at the end of traversal.
                self.stateEvents.resetToFront();
                while (self.stateEvents.hasNext()) {
                    StreamEvent s = getStreamEvent(self.stateEvents.next());
                    self.stateEvents.removeCurrent();
                    if (!(withinTime is int && (currentTime - s.timestamp) > withinTime)) {
                        io:println("CompoundPatternProcessor:process:77 -> ", s, "|", processorAlias);
                        self.emit(s);
                        promoted = true;
                    }
                }
            }
        }
        io:println("CompoundPatternProcessor:process:84 -> ", event, "|", processorAlias);
        return promoted;
    }

    public function promote(StreamEvent stateEvent, string? processorAlias) {
        io:println("CompoundPatternProcessor:promote:89 -> ", stateEvent, "|", processorAlias);
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
            io:println("CompoundPatternProcessor:evict:105 -> ", stateEvent, "|", processorAlias);
            pProcessor.evict(stateEvent, processorAlias);
            io:println("CompoundPatternProcessor:evict:107 -> ", stateEvent, "|", processorAlias);
        }
    }

    public function emit(StreamEvent stateEvent) {
        io:println("CompoundPatternProcessor:emit:112 -> ", stateEvent);
        self.fulfilledEvents[self.fulfilledEvents.length()] = stateEvent;
    }

    public function flushAndGetFulfilledEvents() returns StreamEvent?[] {
        StreamEvent?[] evts = self.fulfilledEvents;
        self.fulfilledEvents = [];
        return evts;
    }

    public function setPreviousProcessor(AbstractOperatorProcessor processor) {
        self.prevProcessor = processor;
    }

    public function setProcessor(AbstractPatternProcessor processor) {
        self.processor = processor;
        self.processor.setPreviousProcessor(self);
    }

    public function getAlias() returns string {
        string alias = "(";
        AbstractPatternProcessor? pProcessor = self.processor;
        if (pProcessor is AbstractPatternProcessor) {
            alias = alias + pProcessor.getAlias();
        }
        alias = alias + ")";
        int? withinTime = self.withinTimeMillis;
        if (withinTime is int) {
            alias += " within " + withinTime + "ms ";
        }
        return alias;
    }
};

public function createCompoundPatternProcessor(int? withinTimeMillis = ()) returns CompoundPatternProcessor {
    CompoundPatternProcessor compoundPatternProcessor = new(withinTimeMillis);
    return compoundPatternProcessor;
}