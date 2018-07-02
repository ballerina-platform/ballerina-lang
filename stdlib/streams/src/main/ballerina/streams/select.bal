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

    private {
        function (StreamEvent[]) nextProcessorPointer;
        Aggregator [] aggregatorArr;
        (function(StreamEvent o) returns string)? groupbyFunc;
        function(StreamEvent o, Aggregator []  aggregatorArr1) returns any selectFunc;
        map <Aggregator []> aggregatorMap;
    }

    new(nextProcessorPointer, aggregatorArr, groupbyFunc, selectFunc) {
    }

    public function process(StreamEvent[] streamEvents) {
                StreamEvent[] newStreamEventArr = [];
                int index =0;

                foreach event in streamEvents {
                    string groupbyKey = groupbyFunc but {
                        (function(StreamEvent o) returns string) groupbyFunction => groupbyFunction(event),
                        () => "DEFAULT"
                    };
                    Aggregator [] aggregatorArray;
                    if(aggregatorMap.hasKey(groupbyKey)){
                        aggregatorArray = aggregatorMap[groupbyKey];
                    } else {
                        int i = 0;
                        foreach aggregator in aggregatorArr {
                            aggregatorArray[i] = aggregator.clone();
                            i++;
                        }
                        aggregatorMap[groupbyKey] = aggregatorArray;
                    }

                    StreamEvent streamEvent = {eventType: event.eventType, timestamp: event.timestamp,
                                                                    eventObject: selectFunc(event, aggregatorArray)};

                    newStreamEventArr[index] = streamEvent;
                    index +=1;
                }

                if (index >0) {
                    nextProcessorPointer(newStreamEventArr);
                }

    }
};

public function createSelect(function (StreamEvent[]) nextProcPointer,
                                Aggregator [] aggregatorArr,
                                (function(StreamEvent o) returns string)? groupbyFunc,
                                function(StreamEvent o, Aggregator [] aggregatorArr1) returns any selectFunc)
        returns Select {

    Select select = new(nextProcPointer, aggregatorArr, groupbyFunc, selectFunc);
    return select;
}