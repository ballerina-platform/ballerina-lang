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

# Processor to perform AND stream operations.
#
# + lhsProcessor - LHS processor of the AND processor
# + rhsProcessor - RHS processor of the AND processor
# + lhsPartialStates - LHS partially promoted states
# + rhsPartialStates - RHS partially promoted states
# + lhsAlias - LHS processor alias
# + rhsAlias - RHS processor alias
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
            boolean promoted = false;
            boolean promote = false;
            boolean tmpPromote = false;
            boolean toNext = false;
            boolean tmpToNext = false;
            // leftward traversal
            AbstractPatternProcessor? lProcessor = self.lhsProcessor;
            if (lProcessor is AbstractPatternProcessor) {
                if (self.rhsPartialStates.length() > 0) {
                    // foreach rhs partial state, copy event data and process in lhsProcessor.
                    string[] originalEvtIds = self.rhsPartialStates.keys();
                    string[] evtIds = <string[]>originalEvtIds.clone();
                    foreach string id in evtIds {
                        StreamEvent partialEvt = <StreamEvent>self.rhsPartialStates[id];
                        partialEvt.addData(event.cloneData());
                        // at the leaf nodes (operand processor), it'll take current events'
                        // stream name into consideration. Therefore, we have to set that.
                        partialEvt.streamName = event.streamName;
                        [tmpPromote, tmpToNext] = lProcessor.process(partialEvt, self.lhsAlias);
                        promote = promote || tmpPromote;
                        toNext = toNext || tmpToNext;
                    }
                } else {
                    [tmpPromote, tmpToNext] = lProcessor.process(event, self.lhsAlias);
                    promote = promote || tmpPromote;
                    toNext = toNext || tmpToNext;
                }
            }
            // if not already promoted or toNext, then do rightward traversal.
            if (!promote || toNext) {
                toNext = false;
                AbstractPatternProcessor? rProcessor = self.rhsProcessor;
                if (rProcessor is AbstractPatternProcessor) {
                    if (self.lhsPartialStates.length() > 0) {
                        // foreach lhs partial state, copy event data and process in rhsProcessor.
                        string[] originalEvtIds = self.lhsPartialStates.keys();
                        string[] evtIds = <string[]>originalEvtIds.clone();
                        foreach string id in evtIds {
                            StreamEvent partialEvt = <StreamEvent>self.lhsPartialStates[id];
                            partialEvt.addData(event.cloneData());
                            // at the leaf nodes (operand processor), it'll take current events'
                            // stream name into consideration. Therefore, we have to set that.
                            partialEvt.streamName = event.streamName;
                            [tmpPromote, tmpToNext] = rProcessor.process(partialEvt, self.rhsAlias);
                            promote = promote || tmpPromote;
                            toNext = toNext || tmpToNext;
                        }
                    } else {
                        [tmpPromote, tmpToNext] = rProcessor.process(event, self.rhsAlias);
                        promote = promote || tmpPromote;
                        toNext = toNext || tmpToNext;
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
            panic error("AND pattern must have a left hand side expression.");
        }
        AbstractPatternProcessor? rProcessor = self.rhsProcessor;
        if (rProcessor is AbstractPatternProcessor) {
            rProcessor.validate();
        } else {
            panic error("AND pattern must have a right hand side expression.");
        }
    }

    # Promotes the `StreamEvent` to the previous processor.
    #
    # + stateEvent - event to promote
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    public function promote(StreamEvent stateEvent, string? processorAlias) {
        string pAlias = <string>processorAlias;
        if (pAlias == self.lhsAlias) {
            // promoted from lhs means, it can be a partial lhs state or a completed state.
            if (self.rhsPartialStates.hasKey(stateEvent.getEventId())) {
                var rhsRemoved = self.rhsPartialStates.remove(stateEvent.getEventId());
                self.stateEvents.addLast(stateEvent);
            } else {
                self.lhsPartialStates[stateEvent.getEventId()] = stateEvent;
            }
        } else {
            // promoted from rhs means, it can be a partial rhs state or a completed state.
            if (self.lhsPartialStates.hasKey(stateEvent.getEventId())) {
                var rhsRemoved = self.lhsPartialStates.remove(stateEvent.getEventId());
                self.stateEvents.addLast(stateEvent);
            } else {
                self.rhsPartialStates[stateEvent.getEventId()] = stateEvent;
            }
        }
    }

    # Evicts the `StreamEvent` from current state branch.
    #
    # + stateEvent - event to promote
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    public function evict(StreamEvent stateEvent, string? processorAlias) {
        // remove matching partial states from this processor.
        any|error removed;
        string pAlias = <string>processorAlias;
        if (pAlias == self.lhsAlias) {
            if (self.lhsPartialStates.hasKey(stateEvent.getEventId())) {
                removed = self.lhsPartialStates.remove(stateEvent.getEventId());
            }
        } else {
            if (self.rhsPartialStates.hasKey(stateEvent.getEventId())) {
                removed = self.rhsPartialStates.remove(stateEvent.getEventId());
            }
        }
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

    # Removes a given `StreamEvent` from the `StateMachine`.
    #
    # + streamEvent - event to be removed
    public function remove(StreamEvent streamEvent) {
        // remove matching partial states from this processor.
        if (self.lhsPartialStates.hasKey(streamEvent.getEventId())) {
            var removed = self.lhsPartialStates.remove(streamEvent.getEventId());
        }
        if (self.rhsPartialStates.hasKey(streamEvent.getEventId())) {
            var removed = self.rhsPartialStates.remove(streamEvent.getEventId());
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

    # Sets a link to the LHS `AbstractOperatorProcessor`.
    #
    # + processor - lhs processor
    public function setLHSProcessor(AbstractPatternProcessor processor) {
        self.lhsProcessor = processor;
        AbstractPatternProcessor? patternProcessor = self.lhsProcessor;
        if (patternProcessor is AbstractPatternProcessor) {
            patternProcessor.setPreviousProcessor(self);
        }
    }

    # Sets a link to the RHS `AbstractOperatorProcessor`.
    #
    # + processor - rhs processor
    public function setRHSProcessor(AbstractPatternProcessor processor) {
        self.rhsProcessor = processor;
        AbstractPatternProcessor? patternProcessor = self.rhsProcessor;
        if (patternProcessor is AbstractPatternProcessor) {
            patternProcessor.setPreviousProcessor(self);
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
            alias += " && " + rProcessor.getAlias();
        } else {
            alias += " && " + self.rhsAlias;
        }
        return alias;
    }
};

# Creates and returns a `AndOperatorProcessor` instance.
#
# + return - A `AndOperatorProcessor` instance.
public function createAndOperatorProcessor() returns AndOperatorProcessor {
    AndOperatorProcessor andOperatorProcessor = new;
    return andOperatorProcessor;
}
