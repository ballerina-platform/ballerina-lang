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
import ballerina/task;
import ballerina/time;

public type NotOperatorProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public AbstractPatternProcessor? processor;
    public task:Scheduler? eventScheduler = ();
    public int forTimeMillis = -1;
    public string? processorAlias = ();

    public function __init(int? forTimeMillis) {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.processor = ();
        self.stateMachine = ();
        if (forTimeMillis is int) {
            self.forTimeMillis = forTimeMillis;
            if (self.forTimeMillis > 0) {
                self.rescheduleNextEvent();
            }
        }
    }

    public function rescheduleNextEvent() {
        // stop (if there's any) running scheduler.
        task:Scheduler? es = self.eventScheduler;
        if (es is task:Scheduler) {
            checkpanic es.stop();
        }
        // init scheduler & start
        self.eventScheduler = new task:Scheduler({
                interval: 0, // interval will be ignored, since the noOfRecurrences is 1
                initialDelay: self.forTimeMillis,
                noOfRecurrences: 1
            }
        );
        checkpanic self.eventScheduler.attach(self.eventSchedulerService, attachment = self);
        checkpanic self.eventScheduler.start();
    }

    public service eventSchedulerService = service {
        resource function onTrigger(NotOperatorProcessor p) {
            StateMachine? stateMachine = p.stateMachine;
            if (stateMachine is StateMachine) {
                int currentTime = time:currentTime().time;
                EventType evntType = "CURRENT";
                map<anydata> data = {};
                // inject an event with current processors' alias as the stream name.
                // So that, it will be identifiable in the process method.
                io:println("NotOperatorProcessor:eventSchedulerService:68 -> Inject to stateMachine");
                stateMachine.inject(new StreamEvent((p.getAlias(), data), evntType, currentTime));
            }
            if (p.forTimeMillis > 0) {
                p.rescheduleNextEvent();
            }
        }
    };

    public function process(StreamEvent event, string? processorAlias) returns (boolean, boolean) {
        io:println("NotOperatorProcessor:process:78 -> ", event, "|", processorAlias);
        boolean promote = false;
        boolean promoted = false;
        boolean isNotProc = true;
        // downward traversal
        AbstractPatternProcessor? processor = self.processor;
        if (processor is AbstractPatternProcessor) {
            // processorAlias is not required when get promoted by
            // its only imidiate descendent. Therefore passing ().
            io:println("NotOperatorProcessor:process:87 -> ", event, "|", processorAlias);
            (promote, isNotProc) = processor.process(event, ());
            io:println("NotOperatorProcessor:process:89 -> ", event, "|", processorAlias);
        }
        // descendents will promote this processor on fulfillment
        if (promote) {
            // since this is NOT operator, if got promoted, we have to evict existing state.
            io:println("NotOperatorProcessor:process:94 -> ", event, "|", processorAlias);
            self.evict(event, processorAlias);
            // once evicted, reschedule the next event.
            if (self.forTimeMillis > 0) {
                io:println("NotOperatorProcessor:process:98 -> ", event, "|", processorAlias);
                self.rescheduleNextEvent();
            }
        } else {
            // else, promote current event to the previous processor.
            AbstractOperatorProcessor? pProcessor = self.prevProcessor;
            if (pProcessor is AbstractOperatorProcessor) {
                io:println("NotOperatorProcessor:process:105 -> ", event, "|", processorAlias);
                pProcessor.promote(event, processorAlias);
                io:println("NotOperatorProcessor:process:107 -> ", event, "|", processorAlias);
                if (self.forTimeMillis > 0) {
                    io:println("NotOperatorProcessor:process:109 -> ", event, "|", processorAlias);
                    self.rescheduleNextEvent();
                }
                promoted = true;
            }
        }
        io:println("NotOperatorProcessor:process:115 -> ", event, "|", processorAlias);
        return (promoted, true);
    }

    public function setStateMachine(StateMachine stateMachine) {
        self.stateMachine = stateMachine;
        AbstractPatternProcessor? processor = self.processor;
        if (processor is AbstractPatternProcessor) {
            processor.setStateMachine(stateMachine);
        }
    }

    public function validate() {
        AbstractOperatorProcessor? pProcessor = self.prevProcessor;
        if (!(pProcessor is AndOperatorProcessor || self.forTimeMillis > 0)) {
            panic error("NOT pattern must be followed by either an AND clause or a effective period.");
        }
        AbstractPatternProcessor? processor = self.processor;
        if (processor is AbstractPatternProcessor) {
            processor.validate();
        } else {
            panic error("NOT pattern must be followed by a valid expression.");
        }
    }

    public function promote(StreamEvent stateEvent, string? processorAlias) {
        // do nothing.
    }

    public function evict(StreamEvent stateEvent, string? processorAlias) {
        // remove matching states from prev processors.
        AbstractOperatorProcessor? pProcessor = self.prevProcessor;
        if (pProcessor is AbstractOperatorProcessor) {
            io:println("NotOperatorProcessor:evict:135 -> ", stateEvent, "|", processorAlias);
            pProcessor.evict(stateEvent, processorAlias);
            io:println("NotOperatorProcessor:evict:137 -> ", stateEvent, "|", processorAlias);
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

public function createNotOperatorProcessor(int? forTimeMillis) returns NotOperatorProcessor {
    NotOperatorProcessor notOperatorProcessor = new(forTimeMillis);
    return notOperatorProcessor;
}