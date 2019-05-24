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

public type OperandProcessor object {
    *AbstractPatternProcessor;
    public (function (map<anydata> stateData) returns boolean)? onConditionFunc;
    public string alias;
    // 1 will be default, and will use -1 for open ranges. (i.e minOccurs 5, maxOccurs -1 means 5 or many)
    public int minOccurs = 1;
    public int maxOccurs = 1;
    public boolean checkRange = false;
    // if there's counting patterns, `lastEvent` will accumulate event data.
    public StreamEvent? lastEvent;

    public function __init(string alias, (function (map<anydata> stateData) returns boolean)? onConditionFunc, int
    minOccurs, int maxOccurs) {
        self.prevProcessor = ();
        self.alias = alias;
        self.onConditionFunc = onConditionFunc;
        self.stateMachine = ();
        self.lockField = 0;
        self.lastEvent = ();
        self.minOccurs = minOccurs;
        self.maxOccurs = maxOccurs;
        self.checkRange = self.minOccurs != 1 || self.maxOccurs != 1;
    }

    public function process(StreamEvent event, string? processorAlias) returns (boolean, boolean) {
        lock {
            self.lockField += 1;
            boolean promote = false;
            boolean promoted = false;
            string streamName = event.getStreamName();
            if (streamName == self.alias) {
                (function (map<anydata> stateData) returns boolean)? condition = self.onConditionFunc;
                if (condition is function (map<anydata> stateData) returns boolean) {
                    if (condition.call(event.data)) {
                        promote = true;
                    }
                } else {
                    // if the condition is not provided.
                    promote = true;
                }
                if (promote) {
                    if (self.checkRange) {
                        // counting patterns
                        StreamEvent? lastEvent = self.lastEvent;
                        if (lastEvent is StreamEvent) {
                            map<anydata>[] lEvtData = lastEvent.dataMap[self.alias] ?: [];
                            lastEvent.dataMap[self.alias] = lEvtData;
                            map<anydata>[] cEvtData = event.dataMap[self.alias] ?: [];
                            event.dataMap[self.alias] = cEvtData;
                            if (cEvtData.length() > 0) {
                                lEvtData[lEvtData.length()] = cEvtData[0];
                            }
                        } else {
                            self.lastEvent = event;
                        }
                        lastEvent = self.lastEvent;
                        if (lastEvent is StreamEvent) {
                            map<anydata>[] evtData = lastEvent.dataMap[self.alias] ?: [];
                            int c = evtData.length();
                            if ((self.minOccurs < 0 || c >= self.minOccurs)
                                && (self.maxOccurs < 0 || c <= self.maxOccurs)) {
                                // promote
                                AbstractOperatorProcessor? pProcessor = self.prevProcessor;
                                if (pProcessor is AbstractOperatorProcessor) {
                                    pProcessor.promote(lastEvent, processorAlias);
                                    promoted = true;
                                }
                            } else if (c > self.maxOccurs) {
                                // remove from stateMachine
                                StateMachine? stateMachine = self.stateMachine;
                                if (stateMachine is StateMachine) {
                                    stateMachine.remove(lastEvent);
                                }
                                self.lastEvent = event;
                            }
                        }
                    } else {
                        AbstractOperatorProcessor? pProcessor = self.prevProcessor;
                        if (pProcessor is AbstractOperatorProcessor) {
                            pProcessor.promote(event, processorAlias);
                            promoted = true;
                        }
                    }
                }
            }
            return (promoted, false);
        }
    }

    public function setStateMachine(StateMachine stateMachine) {
        self.stateMachine = stateMachine;
        stateMachine.register(self);
    }

    public function remove(StreamEvent streamEvent) {
        StreamEvent? lastEvent = self.lastEvent;
        if (lastEvent is StreamEvent) {
            if (lastEvent.getEventId() == streamEvent.getEventId()) {
                self.lastEvent = ();
            }
        }
    }

    public function validate() {
        if (self.alias == "") {
            panic error("Operand must have a valid alias.");
        }
        if (self.checkRange) {
            int i = self.minOccurs;
            int j = self.maxOccurs;
            if (i < 0 && j < 0 || j < i || i == 0 || j == 0) {
                panic error("Pattern must have a valid counting range.");
            }
        }
    }

    public function setPreviousProcessor(AbstractOperatorProcessor processor) {
        self.prevProcessor = processor;
    }

    public function getAlias() returns string {
        return self.alias;
    }
};

public function createOperandProcessor(string alias,
                                       (function (map<anydata> stateData) returns boolean)? onConditionFunc,
                                       int minOccurs = 1, int maxOccurs = 1)
                    returns OperandProcessor {
    OperandProcessor operandProcessor = new(alias, onConditionFunc, minOccurs, maxOccurs);
    return operandProcessor;
}

