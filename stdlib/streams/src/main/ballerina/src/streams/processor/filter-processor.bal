// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
# The ` Filter` object represents the `where` clause in a streaming query. This object takes two parameter for
# initialization. `nextProcessorPointer` is the function pointer of the next processor to be invoked once the
# filtering is complete. conditionFunc is a function pointer which return true if the given where clause evaluates to
# true.
public type Filter object {
    private function (StreamEvent?[]) nextProcessorPointer;
    private function (map<anydata>) returns boolean conditionFunc;

    function __init(function (StreamEvent?[]) nextProcessorPointer,
                    function (map<anydata>) returns boolean conditionFunc) {
        self.nextProcessorPointer = nextProcessorPointer;
        self.conditionFunc = conditionFunc;
    }

    # Process the incoming stream events. This function takes an array of stream events, iterate each of the events
    # in the array, then call the `conditionFunc` on each and see if `conditionFunc` is evaluates to true. if so,
    # those events will be passed to the `nextPrcessorPointer` which can be the `process` function of the next
    # processor object ( e.g. `Select`, `Window`, `Aggregator`.. etc).
    # + streamEvents - The events being filtered.
    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] newStreamEventArr = [];
        int index = 0;
        foreach var ev in streamEvents {
            StreamEvent event = <StreamEvent> ev;
            function (map<anydata>) returns boolean cFunction = self.conditionFunc;
            if (cFunction(event.data)) {
                newStreamEventArr[index] = event;
                index += 1;
            }
        }
        if (index > 0) {
            function (StreamEvent?[]) processorPointer = self.nextProcessorPointer;
            processorPointer(newStreamEventArr);
        }
    }
};

# Creates a `Filter` object and return it.
# + nextProcPointer - The function pointer to the `process` function of the next processor.
# + conditionFunc - The function pointer to the condition evaluator. This is a function which returns true or false
#                   based on the boolean expression given in the where clause.
# + return - Returns a `Filter` object.
public function createFilter(function (StreamEvent?[]) nextProcPointer,
                             function (map<anydata> o) returns boolean conditionFunc) returns Filter {
    Filter filter = new(nextProcPointer, conditionFunc);
    return filter;
}
