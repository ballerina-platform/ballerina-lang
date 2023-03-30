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

import ballerina/test;

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

class PersonObj {
    public int age = 10;
    public string name = "mohan";
    public int year = 2014;
    public string month = "february";
}

class EmployeeObj {
    public int age = 10;
    public string name = "raj";

}

//-----------------------Union Type Stamp -------------------------------------------------------------------

function stampUnionToRecord() returns Employee|error {
    int|float|Employee unionVar = {name: "Raja", status: "single", batch: "LK2014", "school": "Hindu College"};
    Employee|error employee = unionVar.cloneWithType(Employee);
    return employee;
}

function stampUnionToJSON() returns json|error {
    int|float|json unionVar = {name: "Raja", status: "single", batch: "LK2014", school: "Hindu College"};
    json|error jsonValue = unionVar.cloneWithType(json);
    return jsonValue;
}

type XmlType xml;

function stampUnionToXML() {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;
    xml xmlValue = checkpanic unionVar.cloneWithType(XmlType);
    test:assertEquals(xmlValue, xml `<book>The Lost World</book>`);
}

type IntMap map<int>;

function stampUnionToIntMap() {
    int|float|map<int> unionVar = {"a": 1, "b": 2};
    map<int> mapValue = checkpanic unionVar.cloneWithType(IntMap);
    test:assertEquals(mapValue["a"], 1);
    test:assertEquals(mapValue["b"], 2);
    test:assertEquals(mapValue.length(), 2);
    test:assertEquals((typeof mapValue).toString(), "typedesc IntMap");
}

type EmployeeMap map<Employee>;

function stampUnionToConstraintMap() {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<Teacher> teacherMap = {"a": p1, "b": p2};
    int|float|map<Teacher> unionVar = teacherMap;
    map<Employee> mapValue = checkpanic unionVar.cloneWithType(EmployeeMap);
    test:assertEquals(mapValue["a"], p1);
    test:assertEquals(mapValue["b"], p2);
    test:assertEquals(mapValue.length(), 2);
    test:assertEquals((typeof mapValue).toString(), "typedesc EmployeeMap");
}

function stampUnionToAnydata() returns anydata|error {
    int|float|string|boolean unionValue = "mohan";
    anydata|error anydataValue = unionValue.cloneWithType(anydata);
    return anydataValue;
}

type StringString [string, string];

function stampUnionToTuple() returns [string, string]|error {
    int|float|[string, string] unionVar = ["mohan", "LK2014"];
    [string, string]|error tupleValue = unionVar.cloneWithType(StringString);
    return tupleValue;
}

function stampUnionToAnydataV2() returns anydata|error {
    int|float|string|boolean unionValue = "mohan";
    anydata|error anydataValue = unionValue.cloneWithType(anydata);
    return anydataValue;
}

type UnionTypedesc typedesc<int|float|map<Teacher>>;

function stampUnionToConstraintMapToUnion() returns int|float|map<Teacher>|error {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<Teacher> teacherMap = {"a": p1, "b": p2};
    int|float|map<Teacher> unionVar = teacherMap;
    int|float|map<Teacher>|error mapValue = unionVar.cloneWithType(UnionTypedesc);
    return mapValue;
}
