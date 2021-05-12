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

type XY record {
    int x;
    int y;
};

// any field other than x and y
type NotXY record {
    never x?;
    never y?;
};

function testInferredType(XY xy) returns NotXY {
    var {x: _, y: _, ...extra} = xy;
    return extra;
}

function testInferredResType() {
    NotXY extra = testInferredType({x: 10, y: 20, "foo": "bar"});
    assertEquality(extra["foo"], "bar");
}

function testRecordDestructuring1() {
    var {s: s, i: i, ...rest} = recordReturningFunc(10);
    int num1 = <int>rest.get("f");
    assertEquality(num1, 10);
    {s: s, i: i, ...rest} = recordReturningFunc((20));
    int num2 = <int>rest["f"];
    assertEquality(num2, 20);
}

function recordReturningFunc(int num) returns record { string s; int? i; } {
    return {s: "hello", i: 10, "f": num};
}

type Person record {|
    string name;
    int age;
    string...;
|};

function testRecordDestructuring2() {
    string empName;
    int age;
    map<string> details;
    {name: empName, age, ...details} = <Person>{name: "Jane Doe", age: 10, "foo": "bar"};
    assertEquality("bar", details["foo"]);
}

type StudentRecord record {
    int? Id;
    string studentName;
};

public record {
    int Id;
    string studentName;
} {Id, ...studentDetail} = {Id: 1001, studentName: "John", "Age": 24, "surName": "Paker"};

public function testRecordDestructuring3() {
    record {
        int? Id;
        string studentName;
    } {Id, ...studentDetail} = getStudentRecord(1);

    assertEquality("John", <string>studentDetail["studentName"]);
    assertEquality(24, <int>studentDetail["Age"]);
    assertEquality("Paker", <string>studentDetail["surName"]);
}

function getStudentRecord(int? id) returns record { int? Id; string studentName; } {
    return {Id: id, studentName: "John", "Age": 24, "surName": "Paker"};
}

function testRestFieldResolving() {
    testInferredResType();
    testRecordDestructuring1();
    testRecordDestructuring2();
    testRecordDestructuring3();
}

//////////////////////////////////////////////////////////////////////////////////////

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
