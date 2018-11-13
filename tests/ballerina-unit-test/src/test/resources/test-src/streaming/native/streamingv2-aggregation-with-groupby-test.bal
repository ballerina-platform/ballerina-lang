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

    int count = 0;
    while(true) {
        runtime:sleep(500);
        count += 1;
        if((lengthof outputDataArray) == 5 || count == 10) {
            break;
        }
    }

    return outputDataArray;
}

function streamFunc() {

    function (map[]) outputFunc = function (map[] events) {
        foreach m in events {
            // just cast input map into the output type
            OutputRecord o = check <OutputRecord>m;
            outputStream.publish(o);
        }
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
        [function (streams:StreamEvent e) returns string {
            return <string>e.data["inputStream.category"];
        }],
        function (streams:StreamEvent e, streams:Aggregator[] aggregatorArray) returns map {
            streams:Sum iSumAggregator1 = check <streams:Sum>aggregatorArray[0];
            streams:Sum fSumAggregator1 = check <streams:Sum>aggregatorArray[1];
            streams:Count countAggregator1 = check <streams:Count>aggregatorArray[2];
            streams:Average iAvgAggregator1 = check <streams:Average>aggregatorArray[3];
            streams:Average fAvgAggregator1 = check <streams:Average>aggregatorArray[4];
            streams:DistinctCount dCountAggregator1 = check <streams:DistinctCount>aggregatorArray[5];
            streams:StdDev stdDevAggregator1 = check <streams:StdDev>aggregatorArray[6];
            streams:MaxForever iMaxForeverAggregator1 = check <streams:MaxForever>aggregatorArray[7];
            streams:MaxForever fMaxForeverAggregator1 = check <streams:MaxForever>aggregatorArray[8];
            streams:MinForever iMinForeverAggregator1 = check <streams:MinForever>aggregatorArray[9];
            streams:MinForever fMinForeverAggregator1 = check <streams:MinForever>aggregatorArray[10];
            streams:Max iMaxAggregator1 = check <streams:Max>aggregatorArray[11];
            streams:Max fMaxAggregator1 = check <streams:Max>aggregatorArray[12];
            streams:Min iMinAggregator1 = check <streams:Min>aggregatorArray[13];
            streams:Min fMinAggregator1 = check <streams:Min>aggregatorArray[14];

            // got rid of type casting
            return {
                "id": e.data["inputStream.id"],
                "category": e.data["inputStream.category"],
                "iSum": iSumAggregator1.process(e.data["inputStream.intVal"], e.eventType),
                "fSum": fSumAggregator1.process(e.data["inputStream.floatVal"], e.eventType),
                "count": countAggregator1.process((), e.eventType),
                "iAvg": iAvgAggregator1.process(e.data["inputStream.intVal"], e.eventType),
                "fAvg": fAvgAggregator1.process(e.data["inputStream.floatVal"], e.eventType),
                "distCount": dCountAggregator1.process(e.data["inputStream.id"], e.eventType),
                "stdDev": stdDevAggregator1.process(e.data["inputStream.floatVal"], e.eventType),
                "iMaxForever": iMaxForeverAggregator1.process(e.data["inputStream.intVal"], e.eventType),
                "fMaxForever": fMaxForeverAggregator1.process(e.data["inputStream.floatVal"], e.eventType),
                "iMinForever": iMinForeverAggregator1.process(e.data["inputStream.intVal"], e.eventType),
                "fMinForever": fMinForeverAggregator1.process(e.data["inputStream.floatVal"], e.eventType),
                "iMax": iMaxAggregator1.process(e.data["inputStream.intVal"], e.eventType),
                "fMax": fMaxAggregator1.process(e.data["inputStream.floatVal"], e.eventType),
                "iMin": iMinAggregator1.process(e.data["inputStream.intVal"], e.eventType),
                "fMin": fMinAggregator1.process(e.data["inputStream.floatVal"], e.eventType)
            };
        }
    );

    streams:Filter filter = streams:createFilter(select.process, function (map m) returns boolean {
            // simplify filter
            return check <int>m["inputStream.intVal"] > getValue();
        }
    );

    inputStream.subscribe(function (InputRecord i) {
            // make it type unaware and proceed
            map keyVal = <map>i;
            streams:StreamEvent[] eventArr = streams:buildStreamEvent(keyVal, "inputStream");
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
