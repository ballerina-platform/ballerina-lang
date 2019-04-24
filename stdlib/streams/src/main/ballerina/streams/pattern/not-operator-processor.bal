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

public type NotOperatorProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public AbstractPatternProcessor? processor;

    public function __init() {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.processor = ();
    }

    public function process(StreamEvent event, string? processorAlias) returns boolean {
        boolean promote = false;
        boolean promoted = false;
        return promoted;
    }

    public function promote(StreamEvent stateEvent, string? processorAlias) {

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
            io:println("NotOperatorProcessor:evict:50 -> ", stateEvent, "|", processorAlias);
            pProcessor.evict(stateEvent, processorAlias);
            io:println("NotOperatorProcessor:evict:52 -> ", stateEvent, "|", processorAlias);
        }
    }

    public function setPreviousProcessor(AbstractOperatorProcessor processor) {
        self.prevProcessor = processor;
    }

    public function setProcessor(AbstractPatternProcessor processor) {
        self.processor = processor;
        self.processor.setPreviousProcessor(self);
    }

    public function getAlias() returns string {
        string alias = "!";
        AbstractPatternProcessor? pProcessor = self.processor;
        if (pProcessor is AbstractPatternProcessor) {
            alias = alias + pProcessor.getAlias();
        }
        return alias;
    }
};

public function createNotOperatorProcessor() returns NotOperatorProcessor {
    NotOperatorProcessor notOperatorProcessor = new;
    return notOperatorProcessor;
}