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

    private function (StreamEvent[]) nextProcessorPointer;
    private Aggregator [] aggregatorArr;
    private ((function(StreamEvent o) returns string) []) groupbyFuncArray;
    private function(StreamEvent o, Aggregator []  aggregatorArr1) returns map selectFunc;
    private map<Aggregator[]> aggregatorsCloneMap;


    new(nextProcessorPointer, aggregatorArr, groupbyFuncArray, selectFunc) {
        self.aggregatorsCloneMap = {};
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent[] outputStreamEvents = [];
        if (self.aggregatorArr.length() > 0) {
            map<StreamEvent> groupedEvents = {};
            foreach event in streamEvents {

                if (event.eventType == RESET) {
                    self.aggregatorsCloneMap.clear();
                }

                string groupbyKey;
                match self.groupbyFuncArray {
                    (function(StreamEvent o) returns string) [] groupbyFunctionArray => {
                        groupbyKey = self.getGroupByKey(groupbyFunctionArray, event);
                    }
                }

                Aggregator[] aggregatorsClone = [];
                match (self.aggregatorsCloneMap[groupbyKey]) {
                    Aggregator[] aggregators => {
                        aggregatorsClone = aggregators;
                    }
                    () => {
                        int i = 0;
                        foreach aggregator in self.aggregatorArr {
                            aggregatorsClone[i] = aggregator.copy();
                            i += 1;
                        }
                        self.aggregatorsCloneMap[groupbyKey] = aggregatorsClone;
                    }
                }
                StreamEvent e = new ((OUTPUT, self.selectFunc(event, aggregatorsClone)), event.eventType, event
                    .timestamp);
                groupedEvents[groupbyKey] = e;
            }
            foreach key in groupedEvents.keys() {
                match groupedEvents[key] {
                    StreamEvent e => {
                        outputStreamEvents[outputStreamEvents.length()] = e;
                    }
                    () => {}
                }
            }
        } else {
            foreach event in streamEvents {
                StreamEvent e = new ((OUTPUT, self.selectFunc(event, self.aggregatorArr)), event.eventType,
                    event.timestamp);
                outputStreamEvents[outputStreamEvents.length()] = e;
            }
        }
        if (outputStreamEvents.length() > 0) {
            self.nextProcessorPointer(outputStreamEvents);
        }
    }

    public function getGroupByKey((function(StreamEvent o) returns string) [] groupbyFunctionArray, StreamEvent e)
                        returns string {
        string key = "";
        foreach func in groupbyFunctionArray {
            key += func(e);
            key += ",";
        }
        return key;
    }
};

public function createSelect(function (StreamEvent[]) nextProcPointer,
                             Aggregator[] aggregatorArr,
                             ((function (StreamEvent o) returns string)[]) groupbyFuncArray,
                             function (StreamEvent o, Aggregator[] aggregatorArr1) returns map selectFunc)
                    returns Select {

    Select select = new(nextProcPointer, aggregatorArr, groupbyFuncArray, selectFunc);
    return select;
}
