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

import ballerina/task;
import ballerina/time;

# Processor to perform NOT stream operations.
public type NotOperatorProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public int forTimeMillis = -1;
    public AbstractPatternProcessor? processor;
    public task:Scheduler? eventScheduler = ();
    public string? processorAlias = ();
    public int schedulerLock = -1;

    public function __init(int? forTimeMillis) {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.processor = ();
        self.stateMachine = ();
        self.lockField = 0;
        if (forTimeMillis is int) {
            self.forTimeMillis = forTimeMillis;
            if (self.forTimeMillis > 0) {
                self.rescheduleNextEvent(());
            }
        }
    }

    # Reschedules the next NOT event.
    #
    # + delay - delay for the next event
    public function rescheduleNextEvent(int? delay) {
        lock {
            self.schedulerLock += 1;
            int initDelay = (delay is int) ? delay : self.forTimeMillis;
            if (initDelay > 0) {
                // stop (if there's any) running scheduler.
                task:Scheduler? es = self.eventScheduler;
                if (es is task:Scheduler) {
                    checkpanic es.stop();
                }
                // init scheduler & start
                self.eventScheduler = new task:Scheduler({
                        intervalInMillis: 1000, // interval will be ignored, since the noOfRecurrences is 1
                        initialDelayInMillis: initDelay,
                        noOfRecurrences: 1
                    }
                );

                task:Scheduler? evtScheduler = self.eventScheduler;
                if (evtScheduler is task:Scheduler) {
                    checkpanic evtScheduler.attach(self.eventSchedulerService, self);
                    checkpanic evtScheduler.start();
                }
            }
        }
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
                StreamEvent notEvt = new StreamEvent([p.getAlias(), data], evntType, currentTime);
                // to get rid of event duplication.
                notEvt.eventId = p.getAlias();
                stateMachine.inject(notEvt);
            }
            if (p.forTimeMillis > 0) {
                p.rescheduleNextEvent(());
            }
        }
    };

    # Processes the `StreamEvent`.
    #
    # + event - event to process
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    #
    # + return - a tuple indicating, whether the event is promoted and whether to continue to the next processor.
    public function process(StreamEvent event, string? processorAlias) returns [boolean, boolean] {
        lock {
            self.lockField += 1;
            boolean promote = false;
            boolean promoted = false;
            boolean toNext = false;
            int currentTime = time:currentTime().time;
            AbstractOperatorProcessor? pProcessor = self.prevProcessor;
            if (event.getStreamName() == self.getAlias()) {
                // injected events' stream name and the alias of the processor will be the same
                int timeDiff = currentTime - event.timestamp;
                if (timeDiff > (self.forTimeMillis / 100) && timeDiff < self.forTimeMillis) {
                    // if the time diff is between 1% margin and forTimeMillis, then perform 'for' time correction.
                    timeDiff += self.forTimeMillis;
                    self.rescheduleNextEvent(timeDiff);
                } else {
                    // promote emited event
                    if (pProcessor is AbstractOperatorProcessor) {
                        pProcessor.promote(event, processorAlias);
                        self.rescheduleNextEvent(());
                        promoted = true;
                        toNext = false;
                    }
                }
            } else {
                // normal (non-injected) events.
                AbstractPatternProcessor? processor = self.processor;
                if (processor is AbstractPatternProcessor) {
                    // processorAlias is not required when get promoted by
                    // its only imidiate descendent. Therefore passing ().
                    [promote, toNext] = processor.process(event, ());
                }
                // descendents will promote this processor on state fulfillment
                if (promote) {
                    // since this is NOT operator, if got promoted, we have to evict existing state.
                    self.evict(event, processorAlias);
                    // once evicted, reschedule the next event.
                    if (self.forTimeMillis > 0) {
                        self.rescheduleNextEvent(());
                    }
                } else {
                    // else, promote current event (everything which don't get promoted (with filters,
                    // non matching streams, etc...) to the previous processor.
                    if (pProcessor is AbstractOperatorProcessor) {
                        if (self.forTimeMillis > 0) {
                            // if the `for` time is given, any event that doesn't match the NOT state
                            // will be ignored and sent to the next processor.
                            promoted = false;
                            toNext = true;
                        } else {
                            // create an event without state data to represent NOT state, and promote it.
                            map<anydata> data = {};
                            StreamEvent clone = new StreamEvent([event.streamName, data], event.eventType, event.
                                timestamp);
                            clone.eventId = event.eventId;
                            clone.streamName = event.streamName;
                            clone.timestamp = event.timestamp;
                            pProcessor.promote(clone, processorAlias);
                            promoted = true;
                            toNext = true;
                        }
                    }
                }
            }
            return [promoted, toNext];
        }
    }

    # Set the `StateMachine` to the procesor and it's descendants.
    #
    # + stateMachine - `StateMachine` instance
    public function setStateMachine(StateMachine stateMachine) {
        self.stateMachine = stateMachine;
        stateMachine.register(self);
        AbstractPatternProcessor? processor = self.processor;
        if (processor is AbstractPatternProcessor) {
            processor.setStateMachine(stateMachine);
        }
    }

    # Validates the processor and its configs.
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

    # Promotes the `StreamEvent` to the previous processor.
    #
    # + stateEvent - event to promote
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    public function promote(StreamEvent stateEvent, string? processorAlias) {
        // do nothing.
    }

    # Evicts the `StreamEvent` from current state branch.
    #
    # + stateEvent - event to promote
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    public function evict(StreamEvent stateEvent, string? processorAlias) {
        // remove matching states from prev processors.
        AbstractOperatorProcessor? pProcessor = self.prevProcessor;
        if (pProcessor is AbstractOperatorProcessor) {
            pProcessor.evict(stateEvent, processorAlias);
        }
    }

    # Removes a given `StreamEvent` from the `StateMachine`.
    #
    # + streamEvent - event to be removed
    public function remove(StreamEvent streamEvent) {
        // do nothing.
    }

    # Sets a link to the previous `AbstractOperatorProcessor`.
    #
    # + processor - previous processor
    public function setPreviousProcessor(AbstractOperatorProcessor processor) {
        self.prevProcessor = processor;
    }

    # Sets a link to the descendant `AbstractOperatorProcessor`.
    #
    # + processor - descendant processor
    public function setProcessor(AbstractPatternProcessor processor) {
        self.processor = processor;
        AbstractPatternProcessor? pr = self.processor;
        if (pr is AbstractPatternProcessor) {
            pr.setPreviousProcessor(self);
        }
    }

    # Returns the alias of the current processor.
    #
    # + return - alias of the processor.
    public function getAlias() returns string {
        string alias = "!";
        AbstractPatternProcessor? pProcessor = self.processor;
        if (pProcessor is AbstractPatternProcessor) {
            alias = alias + pProcessor.getAlias() + ((self.forTimeMillis > 0)
            ? " for " + self.forTimeMillis.toString() + "millis " : "");
        }
        return alias;
    }
};

# Creates and returns a `NotOperatorProcessor` instance.
#
# + forTimeMillis - `for` time in milliseconds
#
# + return - A `NotOperatorProcessor` instance.
public function createNotOperatorProcessor(int? forTimeMillis) returns NotOperatorProcessor {
    NotOperatorProcessor notOperatorProcessor = new(forTimeMillis);
    return notOperatorProcessor;
}
