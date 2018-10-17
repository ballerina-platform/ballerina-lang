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
    private (function(StreamEvent o) returns string)? groupbyFunc;
    private function(StreamEvent o, Aggregator []  aggregatorArr1) returns map selectFunc;
    private map<Aggregator[]> aggregatorsCloneMap;


    new(nextProcessorPointer, aggregatorArr, groupbyFunc, selectFunc) {
    }

    public function process(StreamEvent[] streamEvents) {
        StreamEvent[] outputStreamEvents = [];
        if (lengthof aggregatorArr > 0) {
            map<StreamEvent> groupedEvents;
            foreach event in streamEvents {

                if (event.eventType == RESET) {
                    aggregatorsCloneMap.clear();
                }

                string groupbyKey = groupbyFunc but {
                    (function(StreamEvent o) returns string) groupbyFunction => groupbyFunction(event),
                    () => DEFAULT
                };
                Aggregator[] aggregatorsClone;
                match (aggregatorsCloneMap[groupbyKey]) {
                    Aggregator[] aggregators => {
                        aggregatorsClone = aggregators;
                    }
                    () => {
                        int i = 0;
                        foreach aggregator in aggregatorArr {
                            aggregatorsClone[i] = aggregator.clone();
                            i++;
                        }
                        aggregatorsCloneMap[groupbyKey] = aggregatorsClone;
                    }
                }
                StreamEvent e = new ((OUTPUT, selectFunc(event, aggregatorsClone)), event.eventType, event.timestamp);
                groupedEvents[groupbyKey] = e;
            }
            foreach key in groupedEvents.keys() {
                match groupedEvents[key] {
                    StreamEvent e => {
                        outputStreamEvents[lengthof outputStreamEvents] = e;
                    }
                    () => {}
                }
            }
        } else {
            foreach event in streamEvents {
                StreamEvent e = new ((OUTPUT, selectFunc(event, aggregatorArr)), event.eventType, event.timestamp);
                outputStreamEvents[lengthof outputStreamEvents] = e;
            }
        }
        if (lengthof outputStreamEvents > 0) {
            nextProcessorPointer(outputStreamEvents);
        }
    }
};

public function createSelect(function (StreamEvent[]) nextProcPointer,
                                Aggregator [] aggregatorArr,
                                (function(StreamEvent o) returns string)? groupbyFunc,
                                function(StreamEvent o, Aggregator [] aggregatorArr1) returns map selectFunc)
        returns Select {

    Select select = new(nextProcPointer, aggregatorArr, groupbyFunc, selectFunc);
    return select;
}
