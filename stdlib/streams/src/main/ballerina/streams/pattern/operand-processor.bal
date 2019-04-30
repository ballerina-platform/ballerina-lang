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

public type OperandProcessor object {
    *AbstractPatternProcessor;
    public (function (map<anydata> stateData) returns boolean)? onConditionFunc;
    public string alias;

    public function __init(string alias, (function (map<anydata> stateData) returns boolean)? onConditionFunc) {
        self.prevProcessor = ();
        self.alias = alias;
        self.onConditionFunc = onConditionFunc;
    }

    public function process(StreamEvent event, string? processorAlias) returns boolean {
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
                AbstractOperatorProcessor? pProcessor = self.prevProcessor;
                if (pProcessor is AbstractOperatorProcessor) {
                    pProcessor.promote(event, processorAlias);
                    promoted = true;
                }
            }
        }
        return promoted;
    }

    public function setStateMachine(StateMachine stateMachine) {
        self.stateMachine = stateMachine;
    }

    public function setPreviousProcessor(AbstractOperatorProcessor processor) {
        self.prevProcessor = processor;
    }

    public function getAlias() returns string {
        return self.alias;
    }
};

public function createOperandProcessor(string alias,
                                       (function (map<anydata> stateData) returns boolean)? onConditionFunc)
                    returns OperandProcessor {
    OperandProcessor operandProcessor = new(alias, onConditionFunc);
    return operandProcessor;
}

