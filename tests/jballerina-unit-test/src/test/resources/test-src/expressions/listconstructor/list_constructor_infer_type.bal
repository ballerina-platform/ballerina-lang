// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type FooListInfer record {
    string s;
    int i = 1;
};

class BarListInfer {
    int i;

    public function init(int i) {
        self.i = i;
    }
}

FooListInfer f = {s: "hello"};
BarListInfer b = new (1);
json j = 1;

function inferSimpleTuple() {
    var x = [1, 2.0, 3.0d, false];
    typedesc<any> ta = typeof x;
    assertEquality("typedesc [int,float,decimal,boolean]", ta.toString());
}

function inferStructuredTuple() {
    var x = [f, b, xml `text`, j];
    typedesc<any> ta = typeof x;
    assertEquality("typedesc [FooListInfer,BarListInfer,lang.xml:Text,json]", ta.toString());
}

function inferNestedTuple() {
    int[2] arr = [1, 2];
    var x = [1, 2.0d, [3, f, [b, b]], arr, j];
    typedesc<any> ta = typeof x;
    assertEquality("typedesc [int,decimal,[int,FooListInfer,[BarListInfer,BarListInfer]],int[2],json]", ta.toString());
}

function testInferSameRecordsInTuple() {
    var arr = [
        {id: 123, name: "Anne", city: "Colombo"},
        {id: 456, name: "Jo", city: "Colombo"}
    ];

    record {|int id; string name; string city;|} rec1 = arr[0];
    assertEquality(123, rec1.id);
    assertEquality("Anne", rec1.name);
    assertEquality("Colombo", rec1.city);

    record {|int id; string name; string city;|} rec2 = arr[1];
    assertEquality(456, rec2.id);
    assertEquality("Jo", rec2.name);
    assertEquality("Colombo", rec2.city);
}

function testInferDifferentRecordsInTuple() {
    var arr = [
        {id: 123, name: "Anne", city: "Colombo"},
        {id: 456, name: "Jo", age: 40}
    ];

    record {|int id; string name; string city;|} rec1 = arr[0];
    assertEquality(123, rec1.id);
    assertEquality("Anne", rec1.name);
    assertEquality("Colombo", rec1.city);

    record {|int id; string name; int age;|} rec2 = arr[1];
    assertEquality(456, rec2.id);
    assertEquality("Jo", rec2.name);
    assertEquality(40, rec2.age);
}

function testInferringForReadOnly() {
    string str = "hello";
    boolean b = true;

    readonly rd1 =[1, str];

    assertEquality(true, rd1 is [int, string] & readonly);
    [int, string] arr1 = <[int, string] & readonly> checkpanic rd1;

    var fn = function() {
        arr1[0] = 12;
    };
    error? res = trap fn();
    assertEquality(true, res is error);

    error err = <error> res;
    assertEquality("modification not allowed on readonly value", err.detail()["message"]);

    FooListInfer & readonly foo = {
        s: "May",
        i: 20
    };
    readonly rd2 = [1, [b, false], foo, foo];

    assertEquality(true, rd2 is [int, [boolean, boolean], FooListInfer, FooListInfer & readonly] & readonly);
    assertEquality(false, rd2 is [int, [boolean, boolean], object {} & readonly, FooListInfer & readonly] & readonly);
    [int, [boolean, boolean], FooListInfer, FooListInfer] arr2 = <[int, [boolean, boolean], FooListInfer, FooListInfer] & readonly> checkpanic rd2;

    fn = function() {
        arr2[0] = 2;
    };
    res = trap fn();
    assertEquality(true, res is error);

    err = <error> res;
    assertEquality("modification not allowed on readonly value", err.detail()["message"]);
}

function testInferringForReadOnlyInUnion() {
    FooListInfer & readonly foo = {
        s: "May",
        i: 20
    };
    boolean b = true;

    readonly|(FooListInfer|int)[] rd = [1, [b, false], foo, foo];

    assertEquality(true, rd is [int, [boolean, boolean], FooListInfer, FooListInfer & readonly] & readonly);
    assertEquality(false, rd is [int, [boolean, boolean], object {} & readonly, FooListInfer & readonly] & readonly);
    [int, [boolean, boolean], FooListInfer, FooListInfer] arr = <[int, [boolean, boolean], FooListInfer, FooListInfer] & readonly> checkpanic rd;

    var fn = function() {
        arr[0] = 2;
    };
    error? res = trap fn();
    assertEquality(true, res is error);

    error err = <error> res;
    assertEquality("modification not allowed on readonly value", err.detail()["message"]);
}

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
