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

type Student record {|
    string name;
    string status;
    string batch;
    string school;
|};

type Employee record {
    string name;
    string status;
    string batch;
};

type Person record {|
    string name;
    string status;
    string batch;
    string school;
|};

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

//----------------------------JSON Stamp -------------------------------------------------------------

function stampJSONToAnydata() returns anydata|error {
    json jsonValue = 3;
    anydata|error anydataValue = anydata.constructFrom(jsonValue);

    return anydataValue;
}

function stampJSONToAnydataV2() returns anydata|error {
    json jsonValue = [1, false, null, "foo", { first: "John", last: "Pala" }];
    anydata|error anyValue = anydata.constructFrom(jsonValue);

    return anyValue;
}

function stampJSONToRecord() returns Employee|error {
    json employee = { name: "John", status: "single", batch: "LK2014" };
    Employee|error employeeValue = Employee.constructFrom(employee);

    return employeeValue;
}

function stampJSONToRecordV2() returns Employee|error {
    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    Employee|error employeeValue = Employee.constructFrom(employee);

    return employeeValue;
}

function stampJSONToJSON() returns json|error {
    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    json|error jsonValue = json.constructFrom(employee);

    return jsonValue;
}

function stampJSONToMap() returns map<anydata>|error {
    json employee = { name: "John", status: "single", batch: "LK2014", school: "Hindu College" };
    map<anydata>|error mapValue = map<anydata>.constructFrom(employee);

    return mapValue;
}

function stampJSONToMapV2() returns map<anydata>|error {
    json teacher = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College",
        emp : { name: "John", status: "single", batch: "LK2014"} };
    map<anydata>|error mapValue = map<anydata>.constructFrom(teacher);

    return mapValue;
}

function stampJSONArrayToConstraintArray() returns Student []|error{
    json employeeArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
                            { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];

    Student []|error studentArray = Student[].constructFrom(employeeArray);

    return studentArray;
}

function stampJSONArrayToAnyTypeArray() returns anydata []|error{
    json jsonArray =  [1, false, "foo", { first: "John", last: "Pala" }];
    anydata[]|error anydataArray = anydata[].constructFrom(jsonArray);

    return anydataArray;
}

function stampJSONToAnydataV3() returns anydata|error {
    json jsonValue = { name: "John", status: "single", batch: "LK2014" };
    anydata|error anydataValue = anydata.constructFrom(jsonValue);

    return anydataValue;
}

function stampJSONToUnion() returns anydata[]|map<anydata>|error {
    json jsonValue = { name: "John", status: "single", batch: "LK2014" };
    anydata[]|map<anydata>|error outputValue = anydata[]|map<anydata>.constructFrom(jsonValue);

    return outputValue;
}

function stampJSONArrayWithNullToAnydataArray() returns anydata []|error{
    json jsonArray =  [1, false, "foo", (), { first: "John", last: "Pala" }];
    anydata[]|error anydataArray = anydata[].constructFrom(jsonArray);

    return anydataArray;
}

type Foo record{|
    string [] a;
|};

function stampJSONToRecordWithArray() returns Foo|error {
    json j1 = {a:["a", "b"]};
    Foo|error returnValue = Foo.constructFrom(j1);

    return returnValue;
}

//----------------------------- Negative Test cases ---------------------------------------------------------------

function stampJSONToRecordNegative() returns Student|error {
    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    Student|error student = Student.constructFrom(employee);

    return student;
}

function stampJSONToMapNegative() returns map<string>|error {
    json employee = { name: "John", age : 23, status: "single", batch: "LK2014", school: "Hindu College" };
    map<string>|error mapValue = map<string>.constructFrom(employee);

    return mapValue;
}

type StringArray record {|
    string[]? a;
|};

function stampNullJSONToArrayNegative() returns StringArray|error {

    json j = ();
    var s = StringArray.constructFrom(j);

    return s;
}
