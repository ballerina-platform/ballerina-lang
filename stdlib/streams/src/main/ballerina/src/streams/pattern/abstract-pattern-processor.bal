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

# Abstract processor encapsulating pattern processor functions.
#
# + prevProcessor - link to the previous processor
# + stateMachine - link to the state machine
# + lockField - lock object to be used with lock blocks
public type AbstractPatternProcessor abstract object {
    public AbstractOperatorProcessor? prevProcessor;
    public StateMachine? stateMachine;
    public int lockField;

    # Processes the `StreamEvent`.
    #
    # + event - event to process
    # + processorAlias - alias for the calling processor, for identification purposes (lhs, rhs).
    #
    # + return - a tuple indicating, whether the event is promoted and whether to continue to the next processor.
    public function process(StreamEvent event, string? processorAlias) returns [boolean, boolean];

    # Sets a link to the previous `AbstractOperatorProcessor`.
    #
    # + processor - previous processor
    public function setPreviousProcessor(AbstractOperatorProcessor processor);

    # Returns the alias of the current processor.
    #
    # + return - alias of the processor.
    public function getAlias() returns string;

    # Set the `StateMachine` to the procesor and it's descendants.
    #
    # + stateMachine - `StateMachine` instance
    public function setStateMachine(StateMachine stateMachine);

    # Validates the processor and its configs.
    public function validate();

    # Removes a given `StreamEvent` from the `StateMachine`.
    #
    # + streamEvent - event to be removed
    public function remove(StreamEvent streamEvent);
};
