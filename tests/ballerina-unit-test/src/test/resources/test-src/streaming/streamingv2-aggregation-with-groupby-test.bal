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

import ballerina/runtime;
import ballerina/streams;
import ballerina/reflect;

type InputRecord record {
    string id;
    string category;
    int intVal;
    float floatVal;
};

type OutputRecord record {
    string id;
    string category;
    int iSum;
    float fSum;
    int count;
    float iAvg;
    float fAvg;
    int distCount;
    float stdDev;
    int iMaxForever;
    float fMaxForever;
    int iMinForever;
    float fMinForever;
    int iMax;
    float fMax;
    int iMin;
    float fMin;
};

stream<InputRecord> inputStream;
stream<OutputRecord> outputStream;
int index = 0;
OutputRecord[] outputDataArray = [];

function startAggregationQuery() returns (OutputRecord[]) {

    InputRecord[] records = [];
    records[0] = { id: "ANX_1", category: "ANX", intVal: 4, floatVal: 4.5 };
    records[1] = { id: "ANX_2", category: "ANX", intVal: 2, floatVal: 2.5 };
    records[2] = { id: "BMX_1", category: "BMX", intVal: 1, floatVal: 1.5 };
    records[3] = { id: "BMX_2", category: "BMX", intVal: 3, floatVal: 3.5 };
    records[4] = { id: "BMX_3", category: "BMX", intVal: 3, floatVal: 8.9 };

    streamFunc();

    outputStream.subscribe(addToOutputDataArray);
    foreach r in records {
        inputStream.publish(r);
    }

    runtime:sleep(1000);
    return outputDataArray;
}

function streamFunc() {

    function (OutputRecord[]) outputFunc = (OutputRecord[] o) => {
        outputStream.publish(o);
    };

    // register output function
    streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);

    // create aggregators
    streams:Sum iSumAggregator = new();
    streams:Sum fSumAggregator = new();
    streams:Count countAggregator = new();
    streams:Average iAvgAggregator = new();
    streams:Average fAvgAggregator = new();
    streams:DistinctCount dCountAggregator = new();
    streams:StdDev stdDevAggregator = new();
    streams:MaxForever iMaxForeverAggregator = new();
    streams:MaxForever fMaxForeverAggregator = new();
    streams:MinForever iMinForeverAggregator = new();
    streams:MinForever fMinForeverAggregator = new();
    streams:Max iMaxAggregator = new();
    streams:Max fMaxAggregator = new();
    streams:Min iMinAggregator = new();
    streams:Min fMinAggregator = new();

    streams:Aggregator[] aggregators = [];
    aggregators[0] = iSumAggregator;
    aggregators[1] = fSumAggregator;
    aggregators[2] = countAggregator;
    aggregators[3] = iAvgAggregator;
    aggregators[4] = fAvgAggregator;
    aggregators[4] = fAvgAggregator;
    aggregators[5] = dCountAggregator;
    aggregators[6] = stdDevAggregator;
    aggregators[7] = iMaxForeverAggregator;
    aggregators[8] = fMaxForeverAggregator;
    aggregators[9] = iMinForeverAggregator;
    aggregators[10] = fMinForeverAggregator;
    aggregators[11] = iMaxAggregator;
    aggregators[12] = fMaxAggregator;
    aggregators[13] = iMinAggregator;
    aggregators[14] = fMinAggregator;

    // create selector
    streams:Select select = streams:createSelect(
                                outputProcess.process,
                                aggregators,
                                (streams:StreamEvent e) => string {
                                    InputRecord i = check <InputRecord>e.eventObject;
                                    return i.category;
                                },
                                (streams:StreamEvent e, streams:Aggregator[] aggregatorArray) => any {
                                    InputRecord i = check <InputRecord>e.eventObject;
                                    streams:Sum iSumAggregator1 = check <streams:Sum>aggregatorArray[0];
                                    streams:Sum fSumAggregator1 = check <streams:Sum>aggregatorArray[1];
                                    streams:Count countAggregator1 = check <streams:Count>aggregatorArray[2];
                                    streams:Average iAvgAggregator1 = check <streams:Average>aggregatorArray[3];
                                    streams:Average fAvgAggregator1 = check <streams:Average>aggregatorArray[4];
                                    streams:DistinctCount dCountAggregator1 = check <streams:DistinctCount>
                                    aggregatorArray[5];
                                    streams:StdDev stdDevAggregator1 = check <streams:StdDev>aggregatorArray[6];
                                    streams:MaxForever iMaxForeverAggregator1 = check <streams:MaxForever>
                                    aggregatorArray[7];
                                    streams:MaxForever fMaxForeverAggregator1 = check <streams:MaxForever>
                                    aggregatorArray[8];
                                    streams:MinForever iMinForeverAggregator1 = check <streams:MinForever>
                                    aggregatorArray[9];
                                    streams:MinForever fMinForeverAggregator1 = check <streams:MinForever>
                                    aggregatorArray[10];
                                    streams:Max iMaxAggregator1 = check <streams:Max>aggregatorArray[11];
                                    streams:Max fMaxAggregator1 = check <streams:Max>aggregatorArray[12];
                                    streams:Min iMinAggregator1 = check <streams:Min>aggregatorArray[13];
                                    streams:Min fMinAggregator1 = check <streams:Min>aggregatorArray[14];
                                    OutputRecord o = {
                                        id: i.id,
                                        category: i.category,
                                        iSum: check <int>iSumAggregator1.process(i.intVal, e.eventType),
                                        fSum: check <float>fSumAggregator1.process(i.floatVal, e.eventType),
                                        count: check <int>countAggregator1.process((), e.eventType),
                                        iAvg: check <float>iAvgAggregator1.process(i.intVal, e.eventType),
                                        fAvg: check <float>fAvgAggregator1.process(i.floatVal, e.eventType),
                                        distCount: check <int>dCountAggregator1.process(i.id, e.eventType),
                                        stdDev: check <float>stdDevAggregator1.process(i.floatVal, e.eventType),
                                        iMaxForever: check <int>iMaxForeverAggregator1.process(i.intVal, e.eventType),
                                        fMaxForever: check <float>fMaxForeverAggregator1.process(i.floatVal, e.eventType
                                        ),
                                        iMinForever: check <int>iMinForeverAggregator1.process(i.intVal, e.eventType),
                                        fMinForever: check <float>fMinForeverAggregator1.process(i.floatVal, e.eventType
                                        ),
                                        iMax: check <int>iMaxAggregator1.process(i.intVal, e.eventType),
                                        fMax: check <float>fMaxAggregator1.process(i.floatVal, e.eventType),
                                        iMin: check <int>iMinAggregator1.process(i.intVal, e.eventType),
                                        fMin: check <float>fMinAggregator1.process(i.floatVal, e.eventType)
                                    };
                                    return o;
                                }
    );

    streams:Filter filter = streams:createFilter(
                                select.process,
                                (any o) => boolean {
                                    InputRecord i = check <InputRecord>o;
                                    return i.intVal > getValue();
                                }
    );

    inputStream.subscribe((InputRecord i) => {
            streams:StreamEvent[] eventArr = streams:buildStreamEvent(i);
            filter.process(eventArr);
        }
    );
}

function getValue() returns int {
    return 0;
}

function addToOutputDataArray(OutputRecord e) {
    outputDataArray[index] = e;
    index = index + 1;
}
