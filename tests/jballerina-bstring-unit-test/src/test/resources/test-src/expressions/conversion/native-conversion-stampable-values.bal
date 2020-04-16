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

function testConvertStampRecordToRecord() returns [Person, Employee]|error {
    Person p = { name: "John", age:25, status: "single", batch: "Batch9", school: "ABC College" };
    Employee e = check Employee.constructFrom(p);
    e.name = "Waruna";
    e["age"] =30;
    p.name = "Watson";
    return [p, e];
}

function testConvertStampRecordToJSON() returns [Employee, json]|error {
    Employee e = { name: "Waruna", status: "married", batch: "Batch9", "school": "DEF College" };
    json j = check json.constructFrom(e);
    e.name = "John";
    map<json> nj = <map<json>> j;
    nj["school"] = "ABC College";
    return [e, <json>nj];
}

function testConvertStampRecordToMap() returns [Employee, map<any>]|error {
    Employee e = { name: "John", status: "single", batch: "Batch9", "school": "ABC College" };
    map<anydata> m = check map<anydata>.constructFrom(e);
    m["name"] = "Waruna";
    e.name = "Mike";
    return [e, m];
}

function testConvertStampTupleToMap() returns [[string, Employee], [string, Employee]]|error {
    [string, Person] tupleValue = ["Waruna", { name: "John", age: 25, status: "single", batch: "Batch9", school:
    "ABC College" }];

    [string, Employee] returnValue = check [string, Employee].constructFrom(tupleValue);
    returnValue[0] = "Chathura";
    tupleValue[0] = "Vinod";
    return [tupleValue, returnValue];
}
