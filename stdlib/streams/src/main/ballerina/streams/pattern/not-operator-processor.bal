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
                self.rescheduleNextEvent(());
            }
        }
    }

    public function rescheduleNextEvent(int? delay) {
        io:println("NotOperatorProcessor:rescheduleNextEvent:43 -> ", delay);
        int initDelay = (delay is int) ? delay : self.forTimeMillis;
        // stop (if there's any) running scheduler.
        task:Scheduler? es = self.eventScheduler;
        if (es is task:Scheduler) {
            checkpanic es.stop();
        }
        // init scheduler & start
        self.eventScheduler = new task:Scheduler({
                interval: 1000, // interval will be ignored, since the noOfRecurrences is 1
                initialDelay: initDelay,
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
                io:println("NotOperatorProcessor:eventSchedulerService:69 -> Inject to stateMachine");
                StreamEvent notEvt = new StreamEvent((p.getAlias(), data), evntType, currentTime);
                // to get rid of event duplication.
                notEvt.eventId = p.getAlias();
                stateMachine.inject(notEvt);
            }
            if (p.forTimeMillis > 0) {
                p.rescheduleNextEvent(());
            }
        }
    };

    public function process(StreamEvent event, string? processorAlias) returns (boolean, boolean) {
        io:println("NotOperatorProcessor:process:80 -> ", event, "|", processorAlias);
        boolean promote = false;
        boolean promoted = false;
        boolean toNext = false;
        int currentTime = time:currentTime().time;
        AbstractOperatorProcessor? pProcessor = self.prevProcessor;

        if (event.getStreamName() == self.getAlias()) {
            // injected events' stream name and the alias of the processor will be the same
            io:println("NotOperatorProcessor:process:89 -> ", event, "|", processorAlias);
            int timeDiff = currentTime - event.timestamp;
            if (timeDiff < self.forTimeMillis) {
                // 'for' time correction
                self.rescheduleNextEvent(timeDiff);
            } else {
                // promote emited event
                if (pProcessor is AbstractOperatorProcessor) {
                    io:println("NotOperatorProcessor:process:97 -> ", event, "|", processorAlias);
                    pProcessor.promote(event, processorAlias);
                    io:println("NotOperatorProcessor:process:99 -> ", event, "|", processorAlias);
                    self.rescheduleNextEvent(());
                    promoted = true;
                    toNext = false;
                }
            }
        } else {
            // normal (non-injected) events.
            io:println("NotOperatorProcessor:process:107 -> ", event, "|", processorAlias);
            AbstractPatternProcessor? processor = self.processor;
            if (processor is AbstractPatternProcessor) {
                // processorAlias is not required when get promoted by
                // its only imidiate descendent. Therefore passing ().
                io:println("NotOperatorProcessor:process:112 -> ", event, "|", processorAlias);
                (promote, toNext) = processor.process(event, ());
                io:println("NotOperatorProcessor:process:114 -> ", event, "|", processorAlias);
            }
            // descendents will promote this processor on state fulfillment
            if (promote) {
                // since this is NOT operator, if got promoted, we have to evict existing state.
                io:println("NotOperatorProcessor:process:119 -> ", event, "|", processorAlias);
                self.evict(event, processorAlias);
                // once evicted, reschedule the next event.
                if (self.forTimeMillis > 0) {
                    io:println("NotOperatorProcessor:process:123 -> ", event, "|", processorAlias);
                    self.rescheduleNextEvent(());
                }
            } else {
                // else, promote current event (everything which don't get promoted (with filters,
                // non matching streams, etc...) to the previous processor.
                io:println("NotOperatorProcessor:process:129 -> ", event, "|", processorAlias);
                if (pProcessor is AbstractOperatorProcessor) {
                    if (self.forTimeMillis > 0) {
                        // if the `for` time is given, any event that doesn't match the NOT state
                        // will be ignored and sent to the next processor.
                        io:println("NotOperatorProcessor:process:134 -> ", event, "|", processorAlias);
                        promoted = false;
                        toNext = true;
                    } else {
                        // create an event without state data to represent NOT state, and promote it.
                        io:println("NotOperatorProcessor:process:139 -> ", event, "|", processorAlias);
                        map<anydata> data = {};
                        StreamEvent clone = new StreamEvent((event.streamName, data), event.eventType, event.timestamp);
                        clone.eventId = event.eventId;
                        clone.streamName = event.streamName;
                        clone.timestamp = event.timestamp;
                        io:println("NotOperatorProcessor:process:145 -> ", clone, "|", processorAlias);
                        pProcessor.promote(clone, processorAlias);
                        io:println("NotOperatorProcessor:process:147 -> ", clone, "|", processorAlias);
                        promoted = true;
                        toNext = true;
                    }
                }
            }
        }
        io:println("NotOperatorProcessor:process:154 -> ", event, "|", processorAlias);
        return (promoted, toNext);
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
            io:println("NotOperatorProcessor:evict:187 -> ", stateEvent, "|", processorAlias);
            pProcessor.evict(stateEvent, processorAlias);
            io:println("NotOperatorProcessor:evict:189 -> ", stateEvent, "|", processorAlias);
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
            alias = alias + pProcessor.getAlias() + ((self.forTimeMillis > 0)
            ? " for " + self.forTimeMillis + "millis " : "");
        }
        return alias;
    }
};

public function createNotOperatorProcessor(int? forTimeMillis) returns NotOperatorProcessor {
    NotOperatorProcessor notOperatorProcessor = new(forTimeMillis);
    return notOperatorProcessor;
}