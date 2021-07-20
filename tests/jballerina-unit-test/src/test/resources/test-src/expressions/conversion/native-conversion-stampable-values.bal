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

type Person record {|
    string name;
    int age;
    string status;
    string batch;
    string school;
|};

type Employee record {
    string name;
    string status;
    string batch;
};

type AnydataMap map<anydata>;
type String_Employee [string, Employee];

function testConvertStampRecordToRecord() returns [Person, Employee]|error {
    Person p = { name: "John", age:25, status: "single", batch: "Batch9", school: "ABC College" };
    Employee e = check p.cloneWithType(Employee);
    e.name = "Waruna";
    e["age"] =30;
    p.name = "Watson";
    return [p, e];
}

function testConvertStampRecordToJSON() returns [Employee, json]|error {
    Employee e = { name: "Waruna", status: "married", batch: "Batch9", "school": "DEF College" };
    json j = check e.cloneWithType(json);
    e.name = "John";
    map<json> nj = <map<json>> j;
    nj["school"] = "ABC College";
    return [e, <json>nj];
}

function testConvertStampRecordToMap() returns [Employee, map<any>]|error {
    Employee e = { name: "John", status: "single", batch: "Batch9", "school": "ABC College" };
    map<anydata> m = check e.cloneWithType(AnydataMap);
    m["name"] = "Waruna";
    e.name = "Mike";
    return [e, m];
}

function testConvertStampTupleToMap() returns [[string, Employee], [string, Employee]]|error {
    [string, Person] tupleValue = ["Waruna", { name: "John", age: 25, status: "single", batch: "Batch9", school:
    "ABC College" }];

    [string, Employee] returnValue = check tupleValue.cloneWithType(String_Employee);
    returnValue[0] = "Chathura";
    tupleValue[0] = "Vinod";
    return [tupleValue, returnValue];
}

type OpenRecord record {

};

type OpenRecordWithUnionTarget record {|
    string|decimal...;
|};

map<json> mp = {
    name: "foo",
    factor: 1.23d
};

function testConvertMapJsonWithDecimalToOpenRecord() {
    var or = mp.cloneWithType(OpenRecord);

    if (or is error) {
        panic error("Invalid Response", detail = "Invalid type `error` recieved from cloneWithType");
    }

    OpenRecord castedValue = <OpenRecord> or;
    assert(castedValue["factor"], mp["factor"]);
    assert(castedValue["name"], mp["name"]);
}

function testConvertMapJsonWithDecimalUnionTarget() {
    var or = mp.cloneWithType(OpenRecordWithUnionTarget);

    if (or is error) {
        panic error("Invalid Response", detail = "Invalid type `error` recieved from cloneWithType");
    }

    OpenRecordWithUnionTarget castedValue = <OpenRecordWithUnionTarget> or;
    assert(castedValue["factor"], mp["factor"]);
    assert(castedValue["name"], mp["name"]);
}

public type Scalar int|string|float|boolean;

public type Argument record {|
    Scalar value;
|};

public function testConvertToUnionWithActualType() {
    json expectedJson = {"value": 132};

    Argument expected = {"value": 132};
    var actual = expectedJson.cloneWithType(Argument);

    if (actual is error) {
        panic error("`cloneWithType` returned an error.");
    }

    assert(actual, expected);
}

function assert(anydata|error actual, anydata|error expected) {
    if (!isEqual(actual, expected)) {
        typedesc<anydata|error> expT = typeof expected;
        typedesc<anydata|error> actT = typeof actual;

        string expectedValAsString = expected is error ? expected.toString() : expected.toString();
        string actualValAsString = actual is error ? actual.toString() : actual.toString();
        string reason = "expected [" + expectedValAsString + "] of type [" + expT.toString()
                            + "], but found [" + actualValAsString + "] of type [" + actT.toString() + "]";

        panic error(reason);
    }
}

isolated function isEqual(anydata|error actual, anydata|error expected) returns boolean {
    if (actual is anydata && expected is anydata) {
        return (actual == expected);
    } else {
        return (actual === expected);
    }
}
