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

# The `Select` object represents the select clause. Anything that comes under select clause like aggregator function
# invocations are also handled in this processor. Further, grouping of the events (provided by the groupby clause) is
# also performed in this processor. `aggregatorArr` is an array of aggregators which are used in the select clause.
# `groupbyFuncArray` is an array of function pointers which returns the values being grouped. `selectFunc` is a
# function pointer to a lambda function which creates the `data` field of the output stream event. `scopeName` is
# used as a breadcrumb to identify the select clause if there are multiple `forever` blocks.
public type Select object {

    private function (StreamEvent?[]) nextProcessorPointer;
    private Aggregator[] aggregatorArr;
    private ((function (StreamEvent o) returns anydata)?[])? groupbyFuncArray;
    private function (StreamEvent o, Aggregator[] aggregatorArr1) returns map<anydata> selectFunc;
    private map<Aggregator[]> aggregatorsCloneMap;
    private string scopeName;

    function __init(function (StreamEvent?[]) nextProcessorPointer, Aggregator[] aggregatorArr,
                    ((function (StreamEvent) returns anydata)?[])? groupbyFuncArray,
                    function (StreamEvent o, Aggregator[] aggregatorArr1) returns map<anydata> selectFunc,
                    string scopeName) {
        self.aggregatorsCloneMap = {};
        self.nextProcessorPointer = nextProcessorPointer;
        self.aggregatorArr = aggregatorArr;
        self.groupbyFuncArray = groupbyFuncArray;
        self.selectFunc = selectFunc;
        self.scopeName = scopeName;
    }

    # Selects only the selected fields in the select clause.
    # + streamEvents - The array of stream events passed to the select clause.
    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] outputStreamEvents = [];
        if (self.aggregatorArr.length() > 0) {
            map<StreamEvent> groupedEvents = {};
            foreach var evt in streamEvents {
                StreamEvent event = <StreamEvent>evt;
                if (event.eventType == RESET) {
                    foreach var [k, v] in self.aggregatorsCloneMap.entries() {
                        boolean stateRemoved = removeState(k);
                    }
                    self.aggregatorsCloneMap.removeAll();
                }

                string groupbyKey = self.getGroupByKey(self.groupbyFuncArray, event);
                Aggregator[] aggregatorsClone = [];
                var aggregators = self.aggregatorsCloneMap[groupbyKey];
                if (aggregators is Aggregator[]) {
                    aggregatorsClone = aggregators;
                } else {
                    int i = 0;
                    foreach var aggregator in self.aggregatorArr {
                        string snapshotableKey = self.scopeName + groupbyKey + "$" + i.toString();
                        Aggregator clone = aggregator.copy();
                        restoreState(snapshotableKey, clone);
                        registerSnapshotable(snapshotableKey, clone);
                        aggregatorsClone[i] = clone;
                        i += 1;
                    }
                    self.aggregatorsCloneMap[groupbyKey] = aggregatorsClone;
                }
                function (StreamEvent o, Aggregator[] aggregatorArr1) returns map<anydata> sFunc = self.selectFunc;
                map<anydata> x = sFunc(event, aggregatorsClone);
                StreamEvent e = new([<@untainted> OUTPUT, x], event.eventType, event.timestamp);
                groupedEvents[groupbyKey] = e;
            }
            foreach var key in groupedEvents.keys() {
                StreamEvent event = <StreamEvent>groupedEvents[key];
                outputStreamEvents[outputStreamEvents.length()] = event;
            }
        } else {
            foreach var evt in streamEvents {
                StreamEvent event = <StreamEvent>evt;
                function (StreamEvent o, Aggregator[] aggregatorArr1) returns map<anydata> sFunc = self.selectFunc;
                StreamEvent e = new([<@untainted> OUTPUT, sFunc(event, self.aggregatorArr)], event.eventType,
                    event.timestamp);
                outputStreamEvents[outputStreamEvents.length()] = e;
            }
        }
        if (outputStreamEvents.length() > 0) {
        function (StreamEvent?[]) processorPointer = self.nextProcessorPointer;
            processorPointer(outputStreamEvents);
        }
    }

    # Creates a unique key for each group with the given values in the group by clause.
    # + groupbyFunctionArray - Function pointer array to the lambda function which returns each group by field.
    # + e - Stream Event object being grouped.
    # + return - Returns a unique groupby key by which the event `e` is grouped.
    public function getGroupByKey(((function (StreamEvent o) returns anydata)?[])? groupbyFunctionArray, StreamEvent e)
                        returns string {
        string key = "";
        if (groupbyFunctionArray is (function (StreamEvent o) returns anydata)?[]) {
            foreach var func in groupbyFunctionArray {
                if (func is (function (StreamEvent o) returns anydata)) {
                    key += func(e).toString();
                    key += ",";
                }
            }
        }
        return key;
    }
};

# Creates and returns a select clause.
# + nextProcPointer - The function pointer to the `process` function of the next processor.
# + aggregatorArr - The array of aggregators used in the select clause. If the same aggregator is used twice, the
#                  `aggregatorArr` will contains that specific aggregator twice in the order they appear in the select
#                   clause.
# + groupbyFuncArray - The array of function pointer which contains the lambda function which returns the expressions
#                      in the group by clause.
# + selectFunc - The function pointer to a lambda function that creates the `data` field of the output stream event.
# + scopeName - A unique id to identify the forever block if there are multiple forever blocks.
# + return - Returns a `Select` object.
public function createSelect(function (StreamEvent?[]) nextProcPointer,
                             Aggregator[] aggregatorArr,
                             ((function (StreamEvent o) returns anydata)?[])? groupbyFuncArray,
                             function (StreamEvent o, Aggregator[] aggregatorArr1) returns map<anydata> selectFunc,
                             string scopeName = "$scope$name")
                    returns Select {

    Select select = new(nextProcPointer, aggregatorArr, groupbyFuncArray, selectFunc, scopeName);
    return select;
}
