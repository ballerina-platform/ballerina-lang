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

type PersonObj object {
    public int age = 10;
    public string name = "mohan";

    public int year = 2014;
    public string month = "february";
};

type EmployeeObj object {
    public int age = 10;
    public string name = "raj";

};

//-----------------------Union Type Stamp -------------------------------------------------------------------

function stampUnionToRecord() returns Employee|error  {
    int|float|Employee unionVar = { name: "Raja", status: "single", batch: "LK2014", "school": "Hindu College" };

    Employee|error  employee = unionVar.cloneWithType(Employee);
    return employee;
}

function stampUnionToJSON() returns json|error {
    int|float|json unionVar = { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" };

    json|error jsonValue = unionVar.cloneWithType(json);
    return jsonValue;
}

function stampUnionToXML() returns xml|error  {
    int|float|xml unionVar = xml `<book>The Lost World</book>`;

    xml|error  xmlValue = unionVar.cloneWithType(xml);
    return xmlValue;
}


function stampUnionToIntMap() returns map<int>|error  {
    int|float|map<int> unionVar = { "a": 1, "b": 2 };

    map<int>|error  mapValue = unionVar.cloneWithType(map<int>);
    return mapValue;
}

function stampUnionToConstraintMap() returns map<Employee>|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };

    int|float|map<Teacher> unionVar = teacherMap;

    map<Employee>|error  mapValue = unionVar.cloneWithType(map<Employee>);
    return mapValue;
}

function stampUnionToAnydata() returns anydata|error {
    int|float|string|boolean unionValue = "mohan";
    anydata|error anydataValue = unionValue.cloneWithType(anydata);

    return anydataValue;
}

function stampUnionToTuple() returns [string, string]|error  {
    int|float|[string, string] unionVar = ["mohan", "LK2014"];
    [string, string]|error  tupleValue = unionVar.cloneWithType([string, string]);

    return tupleValue;
}

function stampUnionToAnydataV2() returns anydata|error {
    int|float|string|boolean unionValue = "mohan";
    anydata|error anydataValue = unionValue.cloneWithType(anydata);

    return anydataValue;
}

function stampUnionToConstraintMapToUnion() returns int|float|map<Teacher>|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };

    int|float|map<Teacher> unionVar = teacherMap;

    int|float|map<Teacher>|error  mapValue = unionVar.cloneWithType(typedesc<int|float|map<Teacher>>);
    return mapValue;
}
