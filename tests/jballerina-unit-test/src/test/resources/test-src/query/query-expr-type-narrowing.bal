// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

public enum Status {
    OPEN,
    CLOSED,
    HOLD
}

public type LiftRecord record {|
    readonly string id;
    string name;
    Status status;
    int capacity;
    boolean night;
    int elevationgain;
|};

public table<LiftRecord> key(id) liftTable = table [
    { id: "astra-express", name: "Astra Express", status: OPEN, capacity: 10, night: false, elevationgain: 20},
    { id: "jazz-cat", name: "Jazz Cat", status: CLOSED, capacity: 5, night: true, elevationgain: 30},
    { id: "jolly-roger", name: "Jolly Roger", status: CLOSED, capacity: 8, night: true, elevationgain: 10}
];

public function testQueryExprWithinTypeGuard() {
    Status? status = OPEN;
    LiftRecord[] lifts = [];
    if status is Status {
        lifts = from var lift in liftTable where lift.status == status select lift;
    }
    assertEquality(1, lifts.length());
    assertEquality(lifts[0].id, "astra-express");
}

function testQueryExprWithinNegatedTypeGuard() {
    assertEquality("hellllo", merge(["h", "e", "l", "l", "o"]));
}

function merge(string[]? tokens) returns string {
    if !(tokens is ()) {
        string s = from var t in tokens
            join var t1 in tokens
            on t equals t1 select t;
        return s;
    }
    return "";
}

type Label record {
    string name;
};

type Data record {
    Label[]? labels;
};

function testTernaryWithinQueryExpression () {
    Label label1 = {name: "A"};
    Label[] labelList = [label1, {name: "B"}];
    Data[] data1 = [{labels: ()}];
    Data[] data2 = [{labels: labelList}];
    Data data3 = {labels: [{name: "John Doe"}]};

    Label[] res1 = getFirstOrDefaultLabel(data2);
    Data[] res2 = filterNonEmtyData(data1);
    Data[] res3 = filterNonEmtyData(data2);
    Data[] res4 = filterCommonLabels(data2);

    assertEquality(label1, res1[0]);
    assertEquality(data3, res2[0]);
    assertEquality(data2, res3);
    assertEquality(data2, res4);
}

function getFirstOrDefaultLabel(Data[] data) returns Label[] {
    Label[] labelList = from Data d in data
        let Label[]? l = d.labels
        select (l != () ? l[0] : {name: "John Doe"});
    return labelList;
}

function filterNonEmtyData (Data[] dataList) returns Data[] {
    Data[] newData = from Data d in dataList
        select {
            labels: let Label[]? l = d.labels
                in (l != () ? from Label nl in l
                    select {name: nl.name} : [{name: "John Doe"}])
        };
    return newData;
}

function filterCommonLabels(Data[] dataList) returns Data[] {
    string[] strArr = ["A", "B", "C"];
    Data[] newData = from Data d in dataList
        select {
            labels: let Label[]? l = d.labels
                in (l != () ? from string str in strArr
                    join Label nl in l on str equals nl.name
                    select {name: nl.name} : [{name: "John Doe"}])
        };
    return newData;
}

//---------------------------------------------------------------------------------------------------------
const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                      message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
