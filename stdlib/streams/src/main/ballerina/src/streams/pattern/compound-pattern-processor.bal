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

import ballerina/time;

# Processor to perform compound stream operations.
#
# + processor - descendant `AbstractPatternProcessor` processor
# + fulfilledEvents - fulfilled states
# + withinTimeMillis - time from initial state to current state should be within this time
public type CompoundPatternProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public AbstractPatternProcessor? processor;
    public StreamEvent?[] fulfilledEvents;
    public int? withinTimeMillis;

    public function __init(int? withinTimeMillis) {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.processor = ();
        self.fulfilledEvents = [];
        self.withinTimeMillis = withinTimeMillis;
        self.stateMachine = ();
        self.lockField = 0;
    }

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
            // downward traversal
            AbstractPatternProcessor? processor = self.processor;
            if (processor is AbstractPatternProcessor) {
                // processorAlias is not required when get promoted by
                // its only imidiate descendent. Therefore passing ().
                [promote, toNext] = processor.process(event, ());
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
                            pProcessor.evict(s, processorAlias);
                        } else {
                            pProcessor.promote(s, processorAlias);
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
                            self.emit(s);
                            promoted = true;
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
        AbstractPatternProcessor? processor = self.processor;
        if (processor is AbstractPatternProcessor) {
            processor.validate();
        } else {
            panic error("Compound operator must have a valid expression.");
        }
    }

    # Promotes the `StreamEvent` to the previous processor.
    #
    # + stateEvent - event to promote
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    public function promote(StreamEvent stateEvent, string? processorAlias) {
        self.stateEvents.addLast(stateEvent);
    }

    # Evicts the `StreamEvent` from current state branch.
    #
    # + stateEvent - event to promote
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    public function evict(StreamEvent stateEvent, string? processorAlias) {
        // remove matching fulfilled states from this processor.
        self.remove(stateEvent);
        // remove matching states from prev processor.
        AbstractOperatorProcessor? pProcessor = self.prevProcessor;
        if (pProcessor is AbstractOperatorProcessor) {
            pProcessor.evict(stateEvent, processorAlias);
        }
    }

    # Removes a given `StreamEvent` from the `StateMachine`.
    #
    # + streamEvent - event to be removed
    public function remove(StreamEvent streamEvent) {
        // remove matching fulfilled states from this processor.
        self.stateEvents.resetToFront();
        while (self.stateEvents.hasNext()) {
            StreamEvent s = getStreamEvent(self.stateEvents.next());
            if (streamEvent.getEventId() == s.getEventId()) {
                self.stateEvents.removeCurrent();
            }
        }
    }

    # Emits given `StreamEvent` as a fulfilled event.
    #
    # + stateEvent - event to emit
    public function emit(StreamEvent stateEvent) {
        // remove from stateMachine
        StateMachine? stateMachine = self.stateMachine;
        if (stateMachine is StateMachine) {
            stateMachine.remove(stateEvent);
        }
        self.fulfilledEvents[self.fulfilledEvents.length()] = stateEvent;
    }

    # Returns fulfilled state events and flush returned states from the state machine.
    #
    # + return - an array of `StreamEvent`s.
    public function flushAndGetFulfilledEvents() returns StreamEvent?[] {
        StreamEvent?[] evts = self.fulfilledEvents;
        self.fulfilledEvents = [];
        return evts;
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
        AbstractPatternProcessor? patternProcessor = self.processor;
        if (patternProcessor is AbstractPatternProcessor) {
            patternProcessor.setPreviousProcessor(self);
        }
    }

    # Returns the alias of the current processor.
    #
    # + return - alias of the processor.
    public function getAlias() returns string {
        string alias = "(";
        AbstractPatternProcessor? pProcessor = self.processor;
        if (pProcessor is AbstractPatternProcessor) {
            alias = alias + pProcessor.getAlias();
        }
        alias = alias + ")";
        int? withinTime = self.withinTimeMillis;
        if (withinTime is int) {
            alias += " within " + withinTime.toString() + "ms ";
        }
        return alias;
    }
};

# Creates and returns a `CompoundPatternProcessor` instance.
#
# + withinTimeMillis - time from initial state to current state
#
# + return - A `CompoundPatternProcessor` instance.
public function createCompoundPatternProcessor(public int? withinTimeMillis = ()) returns CompoundPatternProcessor {
    CompoundPatternProcessor compoundPatternProcessor = new(withinTimeMillis);
    return compoundPatternProcessor;
}