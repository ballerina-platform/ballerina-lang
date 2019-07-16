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

# StateMachine which performs stream pattern processing.
#
# + nextProcessorPointer - pointer for the next processor
# + rootProcessor - pointer to the root processor
# + processors - list of processors registered to the `StateMachine`
public type StateMachine object {
    private function (StreamEvent?[]) nextProcessorPointer;
    private CompoundPatternProcessor rootProcessor;
    private AbstractPatternProcessor[] processors = [];

    function __init(CompoundPatternProcessor rootProcessor, function (StreamEvent?[]) nextProcessorPointer) {
        self.nextProcessorPointer = nextProcessorPointer;
        self.rootProcessor = rootProcessor;
        self.rootProcessor.setStateMachine(self);
        self.rootProcessor.validate();
    }

    # Processes the nicoming streamEvents array.
    #
    # + streamEvents - list of stream events
    public function process(StreamEvent?[] streamEvents) {
        foreach var e in streamEvents {
            if (e is StreamEvent) {
                [boolean, boolean] success = self.rootProcessor.process(e, ());
            }
        }
        StreamEvent?[] events = self.rootProcessor.flushAndGetFulfilledEvents();
        function (StreamEvent?[]) nextProcessor = self.nextProcessorPointer;
        nextProcessor(events);
    }

    # Injects a given `StreamEvent` to the `StateMachine`.
    #
    # + streamEvent - event to be injected
    public function inject(StreamEvent streamEvent) {
        StreamEvent?[] streamEvents = [streamEvent];
        self.process(streamEvents);
    }

    # Removes all the matchihng states from the `StateMachine`.
    #
    # + streamEvent - event to be removed
    public function remove(StreamEvent streamEvent) {
        foreach AbstractPatternProcessor p in self.processors {
            p.remove(streamEvent);
        }
    }

    # Register a processor to the `StateMachine`.
    #
    # + processor - processor to register
    public function register(AbstractPatternProcessor processor) {
        self.processors[self.processors.length()] = processor;
    }
};

# Creates and returns a `StateMachine` object.
#
# + rootProcessor - pointer to the root processor
# + nextProcPointer - pointer for the next processor
#
# + return - A `StateMachine` object which performs pattern processing.
public function createStateMachine(CompoundPatternProcessor rootProcessor,
                                   function (StreamEvent?[]) nextProcPointer) returns StateMachine {
    StateMachine stateMachine = new(rootProcessor, nextProcPointer);
    return stateMachine;
}
