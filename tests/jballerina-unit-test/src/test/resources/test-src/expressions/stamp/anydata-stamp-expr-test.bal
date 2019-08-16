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

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

function stampAnydataToJSON() returns json|error  {
    anydata anydataValue = 3;
    json|error  jsonValue = json.constructFrom(anydataValue);

    return jsonValue;
}

function stampAnydataToRecord() returns Employee|error  {
    Teacher t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = t1;
    Employee|error  employee = Employee.constructFrom(anydataValue);
    return employee;
}

function stampAnydataToJSONV2() returns json|error  {
    json t1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = t1;

    json|error  jsonValue = json.constructFrom(anydataValue);
    return jsonValue;
}

function stampAnydataToXML() returns xml|error  {
    anydata anydataValue = xml `<book>The Lost World</book>`;

    xml|error  xmlValue = xml.constructFrom(anydataValue);
    return xmlValue;
}

function stampAnydataToMap() returns map<Employee>|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    map<Teacher> teacherMap = { "a": p1, "b": p2 };
    anydata anydataValue = teacherMap;
    map<Employee>|error  mapValue = map<Employee>.constructFrom(anydataValue);

    return mapValue;
}

function stampAnydataToRecordArray() returns Teacher[]|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata anydataValue = teacherArray;
    Teacher[]|error  returnValue = Teacher[].constructFrom(anydataValue);

    return returnValue;
}

function stampAnydataToTuple() returns [string,Teacher]|error  {
    [string, Teacher]  tupleValue = ["Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" }];

    anydata anydataValue = tupleValue;
    [string,Teacher]|error  returnValue = [string, Teacher].constructFrom(anydataValue);

    return returnValue;
}

function stampAnydataMapToAnydataMap() returns map<anydata>|error {
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    map<anydata>|error mapValue = map<anydata>.constructFrom(anydataMap);
    return mapValue;
}

function stampAnydataToAnydata() returns anydata|error {
    json jsonValue = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    anydata anydataValue = jsonValue;
    anydata|error returnValue = anydata.constructFrom(anydataValue);

    return returnValue;
}

function stampAnydataMapToUnion() returns json|xml|error {
    map<anydata> anydataMap = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };

    json|xml|error mapValue = json|xml.constructFrom(anydataMap);
    return mapValue;
}
