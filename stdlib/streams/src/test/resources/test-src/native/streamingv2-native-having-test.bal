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
};

type OutputRecord record {
    string id;
    string category;
    int sum;
};

stream<InputRecord> inputStream = new;
stream<OutputRecord> outputStream = new;
int index = 0;
OutputRecord[] outputDataArray = [];

public function startHavingQuery() returns any {

    InputRecord[] records = [];
    records[0] = { id: "ANX_2", category: "ANX", intVal: 4 };
    records[1] = { id: "ANX_1", category: "ANX", intVal: 2 };
    records[2] = { id: "BMX_1", category: "BMX", intVal: 1 };
    records[3] = { id: "BMX_2", category: "BMX", intVal: 3 };
    records[4] = { id: "BMX_3", category: "BMX", intVal: 3 };

    streamFunc();

    outputStream.subscribe(function (OutputRecord e) {printInputRecords(e);});
    foreach var r in records {
        inputStream.publish(r);
    }

    int count = 0;
    while (true) {
        runtime:sleep(500);
        count += 1;
        if ((outputDataArray.length()) == 4 || count == 10) {
            break;
        }
    }

    return outputDataArray;
}

function streamFunc() {

    function (map<anydata>[]) outputFunc = function (map<anydata>[] events) {
        foreach var m in events {
            // just cast input map into the output type
            OutputRecord o = <OutputRecord>OutputRecord.stamp(m.clone());
            outputStream.publish(o);
        }
    };

    // register output function
    streams:OutputProcess outputProcess = streams:createOutputProcess(outputFunc);

    // create aggregators
    streams:Sum iSumAggregator = new();

    streams:Aggregator[] aggregators = [];
    aggregators[0] = iSumAggregator;

    streams:Filter outFilter = streams:createFilter(function (streams:StreamEvent?[] e) {outputProcess.process(e);},
        function (map<anydata> m) returns boolean {
            // simplify filter, note the "OUTPUT" prefix
            return <int>m["OUTPUT.sum"] > getValue();
        }
    );

    // create selector
    streams:Select select = streams:createSelect(function (streams:StreamEvent?[] e) { outFilter.process(e);},
        aggregators,
        [function (streams:StreamEvent e) returns anydata {
            return e.data["inputStream.category"];
        }],
        function (streams:StreamEvent e, streams:Aggregator[] aggregatorArray) returns map<anydata> {
            streams:Sum iSumAggregator1 = <streams:Sum>aggregatorArray[0];
            // got rid of type casting
            return {
                "id": e.data["inputStream.id"],
                "category": e.data["inputStream.category"],
                "sum": iSumAggregator1.process(e.data["inputStream.intVal"], e.eventType)
            };
        }
    );

    inputStream.subscribe(function (InputRecord i) {
            // make it type unaware and proceed
            streams:StreamEvent?[] eventArr = streams:buildStreamEvent(i, "inputStream");
            select.process(eventArr);
        }
    );
}

function getValue() returns int {
    return 3;
}

function printInputRecords(OutputRecord e) {
    addToOutputDataArray(e);
}

function addToOutputDataArray(OutputRecord e) {
    outputDataArray[index] = e;
    index = index + 1;
}
