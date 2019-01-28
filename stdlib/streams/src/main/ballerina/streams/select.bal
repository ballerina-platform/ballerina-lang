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

public type Select object {

    private function (StreamEvent?[]) nextProcessorPointer;
    private Aggregator[] aggregatorArr;
    private ((function (StreamEvent o) returns anydata)?[])? groupbyFuncArray;
    private function (StreamEvent o, Aggregator[] aggregatorArr1) returns map<anydata> selectFunc;
    private map<Aggregator[]> aggregatorsCloneMap;

    function __init(function (StreamEvent?[]) nextProcessorPointer, Aggregator[] aggregatorArr,
                    ((function (StreamEvent) returns anydata)?[])? groupbyFuncArray,
                    function (StreamEvent o, Aggregator[] aggregatorArr1) returns map<anydata> selectFunc) {
        self.aggregatorsCloneMap = {};
        self.nextProcessorPointer = nextProcessorPointer;
        self.aggregatorArr = aggregatorArr;
        self.groupbyFuncArray = groupbyFuncArray;
        self.selectFunc = selectFunc;
    }

    public function process(StreamEvent?[] streamEvents) {
        StreamEvent?[] outputStreamEvents = [];
        if (self.aggregatorArr.length() > 0) {
            map<StreamEvent> groupedEvents = {};
            foreach var evt in streamEvents {
                StreamEvent event = <StreamEvent> evt;
                if (event.eventType == RESET) {
                    self.aggregatorsCloneMap.clear();
                }

                string groupbyKey = self.getGroupByKey(self.groupbyFuncArray, event);
                Aggregator[] aggregatorsClone = [];
                var aggregators = self.aggregatorsCloneMap[groupbyKey];
                if (aggregators is Aggregator[]) {
                    aggregatorsClone = aggregators;
                } else {
                    int i = 0;
                    foreach var aggregator in self.aggregatorArr {
                        aggregatorsClone[i] = aggregator.copy();
                        i += 1;
                    }
                    self.aggregatorsCloneMap[groupbyKey] = aggregatorsClone;
                }
                map<anydata> x = self.selectFunc.call(event, aggregatorsClone);
                StreamEvent e = new((OUTPUT, x), event.eventType, event.timestamp);
                groupedEvents[groupbyKey] = e;
            }
            foreach var key in groupedEvents.keys() {
                StreamEvent event = <StreamEvent>groupedEvents[key];
                outputStreamEvents[outputStreamEvents.length()] = event;
            }
        } else {
            foreach var evt in streamEvents {
                StreamEvent event = <StreamEvent> evt;
                StreamEvent e = new((OUTPUT, self.selectFunc.call(event, self.aggregatorArr)), event.eventType,
                    event.timestamp);
                outputStreamEvents[outputStreamEvents.length()] = e;
            }
        }
        if (outputStreamEvents.length() > 0) {
            self.nextProcessorPointer.call(outputStreamEvents);
        }
    }

    public function getGroupByKey(((function (StreamEvent o) returns anydata)?[])? groupbyFunctionArray, StreamEvent e)
                        returns string {
        string key = "";
        if(groupbyFunctionArray is (function (StreamEvent o) returns anydata)?[]) {
            foreach var func in groupbyFunctionArray {
                if (func is (function (StreamEvent o) returns anydata)) {
                    key += <string>func.call(e);
                    key += ",";
                }
            }
        }
        return key;
    }
};

public function createSelect(function (StreamEvent?[]) nextProcPointer,
                             Aggregator[] aggregatorArr,
                             ((function (StreamEvent o) returns anydata)?[])? groupbyFuncArray,
                             function (StreamEvent o, Aggregator[] aggregatorArr1) returns map<anydata> selectFunc)
                    returns Select {

    Select select = new(nextProcPointer, aggregatorArr, groupbyFuncArray, selectFunc);
    return select;
}
