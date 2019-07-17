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

# Processor to perform OR stream operations.
#
# + lhsProcessor - LHS processor of the AND processor
# + rhsProcessor - RHS processor of the AND processor
# + lhsEvicted - LHS partially evicted states
# + rhsEvicted - RHS partially evicted states
# + lhsAlias - LHS processor alias
# + rhsAlias - RHS processor alias
public type OrOperatorProcessor object {
    *AbstractPatternProcessor;
    *AbstractOperatorProcessor;
    public AbstractPatternProcessor? lhsProcessor;
    public AbstractPatternProcessor? rhsProcessor;
    public map<StreamEvent> lhsEvicted;
    public map<StreamEvent> rhsEvicted;
    public string lhsAlias = "lhs";
    public string rhsAlias = "rhs";

    public function __init() {
        self.prevProcessor = ();
        self.stateEvents = new;
        self.lhsProcessor = ();
        self.rhsProcessor = ();
        self.stateMachine = ();
        self.lhsEvicted = {};
        self.rhsEvicted = {};
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
            // leftward traversal
            AbstractPatternProcessor? lProcessor = self.lhsProcessor;
            if (lProcessor is AbstractPatternProcessor) {
                [promote, toNext] = lProcessor.process(event, self.lhsAlias);
            }
            // rightward traversal
            AbstractPatternProcessor? rProcessor = self.rhsProcessor;
            if ((!promote || toNext) && rProcessor is AbstractPatternProcessor) {
                [promote, toNext] = rProcessor.process(event, self.rhsAlias);
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
            return [promoted, toNext];
        }
    }

    # Set the `StateMachine` to the procesor and it's descendants.
    #
    # + stateMachine - `StateMachine` instance
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

    # Validates the processor and its configs.
    public function validate() {
        AbstractPatternProcessor? lProcessor = self.lhsProcessor;
        if (lProcessor is AbstractPatternProcessor) {
            lProcessor.validate();
        } else {
            panic error("OR pattern must have a valid left hand side expression.");
        }
        AbstractPatternProcessor? rProcessor = self.rhsProcessor;
        if (rProcessor is AbstractPatternProcessor) {
            rProcessor.validate();
        } else {
            panic error("OR pattern must have a valid right hand side expression.");
        }
    }

    # Promotes the `StreamEvent` to the previous processor.
    #
    # + stateEvent - event to promote
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    public function promote(StreamEvent stateEvent, string? processorAlias) {
        // if there's partial eviction, clean it beforehand.
        string pAlias = <string>processorAlias;
        any|error cleaned;
        if (pAlias == self.lhsAlias) {
            if (self.rhsEvicted.hasKey(stateEvent.getEventId())) {
                cleaned = self.rhsEvicted.remove(stateEvent.getEventId());
            }
        } else {
            if (self.lhsEvicted.hasKey(stateEvent.getEventId())) {
                cleaned = self.lhsEvicted.remove(stateEvent.getEventId());
            }
        }
        self.stateEvents.addLast(stateEvent);
    }

    # Evicts the `StreamEvent` from current state branch.
    #
    # + stateEvent - event to promote
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    public function evict(StreamEvent stateEvent, string? processorAlias) {
        boolean proceed = false;
        string pAlias = <string>processorAlias;
        // in `or` processor, either side can be true,
        // therefore have to check both sides before evict
        if (pAlias == self.lhsAlias) {
            if(self.rhsEvicted.hasKey(stateEvent.getEventId())) {
                var proceedVal = self.rhsEvicted.remove(stateEvent.getEventId());
                proceed = true;
            }
            if (!proceed) {
                self.lhsEvicted[stateEvent.getEventId()] = stateEvent;
            }
        } else {
            if (self.lhsEvicted.hasKey(stateEvent.getEventId())) {
                var tempVar = self.lhsEvicted.remove(stateEvent.getEventId());
                proceed = true;
            }
            if (!proceed) {
                self.rhsEvicted[stateEvent.getEventId()] = stateEvent;
            }
        }

        if (proceed) {
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
                pProcessor.evict(stateEvent, processorAlias);
            }
        }
    }

    # Removes a given `StreamEvent` from the `StateMachine`.
    #
    # + streamEvent - event to be removed
    public function remove(StreamEvent streamEvent) {
        if (self.rhsEvicted.hasKey(streamEvent.getEventId())) {
            var removed = self.rhsEvicted.remove(streamEvent.getEventId());
        }
        if (self.lhsEvicted.hasKey(streamEvent.getEventId())) {
            var removed = self.lhsEvicted.remove(streamEvent.getEventId());
        }
        // remove matching fulfilled states from this processor.
        self.stateEvents.resetToFront();
        while (self.stateEvents.hasNext()) {
            StreamEvent s = getStreamEvent(self.stateEvents.next());
            if (streamEvent.getEventId() == s.getEventId()) {
                self.stateEvents.removeCurrent();
            }
        }
    }

    # Sets a link to the previous `AbstractOperatorProcessor`.
    #
    # + processor - previous processor
    public function setPreviousProcessor(AbstractOperatorProcessor processor) {
        self.prevProcessor = processor;
    }

    # Sets a link to the lhs `AbstractOperatorProcessor`.
    #
    # + processor - lhs processor
    public function setLHSProcessor(AbstractPatternProcessor processor) {
        self.lhsProcessor = processor;
        AbstractPatternProcessor? pr = self.lhsProcessor;
        if (pr is AbstractPatternProcessor) {
            pr.setPreviousProcessor(self);
        }
    }

    # Sets a link to the rhs `AbstractOperatorProcessor`.
    #
    # + processor - rhs processor
    public function setRHSProcessor(AbstractPatternProcessor processor) {
        self.rhsProcessor = processor;
        AbstractPatternProcessor? pr = self.rhsProcessor;
        if (pr is AbstractPatternProcessor) {
            pr.setPreviousProcessor(self);
        }
    }

    # Returns the alias of the current processor.
    #
    # + return - alias of the processor.
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

# Creates and returns a `OrOperatorProcessor` instance.
#
# + return - A `OrOperatorProcessor` instance.
public function createOrOperatorProcessor() returns OrOperatorProcessor {
    OrOperatorProcessor orOperatorProcessor = new;
    return orOperatorProcessor;
}
