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

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

type IntRecord record {
    int a;
    int b;
};

type AnydataMap map<anydata>;

type StringMap map<string>;

type IntMap map<int>;

type EmployeeMap map<Employee>;

type TeacherMap map<Teacher>;

type JsonMap map<json>;

type EmployeeDoubleMap map<map<Employee>>;

type IntTrippleMap map<map<map<int>>>;

type AnydataTrippleMap map<map<map<anydata>>>;

type MapOrXMLTypeDesc typedesc<map<Teacher>|xml>;

//----------------------------Map Stamp -------------------------------------------------------------

function stampIntMapToRecord() returns IntRecord|error {
    map<int> m = {"a": 1, "b": 2};
    IntRecord|error intRecord = m.cloneWithType(IntRecord);
    return intRecord;
}

function stampIntMapToJSON() returns json|error {
    map<int> m = {"a": 1, "b": 2};
    json|error jsonValue = m.cloneWithType(json);
    return jsonValue;
}

function stampIntMapToAnydata() returns anydata|error {
    map<int> m = {"a": 1, "b": 2};
    anydata|error anydataValue = m.cloneWithType(anydata);
    return anydataValue;
}

function stampIntMapToIntMap() {
    map<int> m = {"a": 1, "b": 2};
    map<int> mapValue = checkpanic m.cloneWithType(IntMap);
    test:assertEquals(mapValue["a"], 1);
    test:assertEquals(mapValue["b"], 2);
    test:assertEquals(mapValue.length(), 2);
    test:assertEquals((typeof mapValue).toString(), "typedesc IntMap");
}

function stampIntMapToAnydataMap() returns map<anydata>|error {
    map<int> m = {"a": 1, "b": 2};
    map<anydata>|error mapValue = m.cloneWithType(AnydataMap);
    return mapValue;
}

function stampAnydataMapToIntMap() returns map<int>|error {
    map<anydata> m = {"a": 1, "b": 2};
    map<int>|error mapValue = m.cloneWithType(IntMap);
    return mapValue;
}

function stampAnydataMapToStringMap() returns map<string>|error {
    map<anydata> m = {firstName: "mohan", lastName: "raj"};
    map<string>|error mapValue = m.cloneWithType(StringMap);
    return mapValue;
}

function stampAnydataMapToStringMapWithoutExplicitConstraintType() returns map<string>|error {
    map<anydata> m = {firstName: "mohan", lastName: "raj"};
    map<string>|error mapValue = m.cloneWithType(StringMap);
    return mapValue;
}

function stampAnydataMapToRecord() returns Teacher|error {
    map<anydata> anydataMap = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher|error teacherValue = anydataMap.cloneWithType(Teacher);
    return teacherValue;
}

function stampAnydataMapToJSON() returns json|error {
    map<anydata> anydataMap = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    json|error jsonValue = anydataMap.cloneWithType(json);
    return jsonValue;
}

function stampAnydataMapToAnydata() returns anydata|error {
    map<anydata> anydataMap = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    anydata|error anydataValue = anydataMap.cloneWithType(anydata);
    return anydataValue;
}

function stampAnydataMapToSimilarOpenRecordMap() returns map<Employee>|error {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<anydata> teacherMap = {"a": p1, "b": p2};
    map<Employee>|error mapValue = teacherMap.cloneWithType(EmployeeMap);
    return mapValue;
}

function stampAnydataMapToRecordMap() {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<anydata> teacherMap = {"a": p1, "b": p2};
    map<Teacher> mapValue = checkpanic teacherMap.cloneWithType(TeacherMap);
    test:assertEquals(mapValue["a"], p1);
    test:assertEquals(mapValue["b"], p2);
    test:assertEquals(mapValue.length(), 2);
    test:assertEquals((typeof mapValue).toString(), "typedesc TeacherMap");
}

function stampAnydataMapToJSONMap() returns map<json>|error {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<anydata> teacherMap = {"a": p1, "b": p2};
    map<json>|error jsonValue = teacherMap.cloneWithType(JsonMap);
    return jsonValue;
}

function stampRecordMapToAnydataMap() returns map<anydata>|error {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<Teacher> teacherMap = {"a": p1, "b": p2};
    map<anydata>|error mapValue = teacherMap.cloneWithType(AnydataMap);
    return mapValue;
}

function stampRecordMapToSimilarOpenRecordMap() returns map<Employee>|error {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<Teacher> teacherMap = {"a": p1, "b": p2};
    map<Employee>|error mapValue = teacherMap.cloneWithType(EmployeeMap);
    return mapValue;
}

function stampRecordMapToJSONMap() {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<Teacher> teacherMap = {"a": p1, "b": p2};
    map<json> mapValue = checkpanic teacherMap.cloneWithType(JsonMap);
    test:assertEquals(mapValue["a"], p1);
    test:assertEquals(mapValue["b"], p2);
    test:assertEquals(mapValue.length(), 2);
    test:assertEquals((typeof mapValue).toString(), "typedesc JsonMap");
}

function stampJSONMapToRecordMap() {
    map<json> teacherMap = {
        "a": {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"},
        "b": {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"}
    };
    map<Employee> mapValue = checkpanic teacherMap.cloneWithType(EmployeeMap);
    test:assertEquals(mapValue["a"]["name"], "Raja");
    test:assertEquals(mapValue["a"]["age"], 25);
    test:assertEquals(mapValue["a"]["status"], "single");
    test:assertEquals(mapValue["a"]["batch"], "LK2014");
    test:assertEquals(mapValue["a"]["school"], "Hindu College");
    test:assertEquals(mapValue["b"]["name"], "Mohan");
    test:assertEquals(mapValue["b"]["age"], 30);
    test:assertEquals(mapValue["b"]["status"], "single");
    test:assertEquals(mapValue["b"]["batch"], "LK2014");
    test:assertEquals(mapValue["b"]["school"], "Hindu College");
    test:assertEquals(mapValue.length(), 2);
    test:assertEquals((typeof mapValue).toString(), "typedesc EmployeeMap");
}

function stampRecordTypeMultiDimensionMap() {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<Teacher> teacherMap = {"a": p1, "b": p2};
    map<map<Teacher>> multiMap = {"aa": teacherMap, "bb": teacherMap};
    map<map<Employee>> mapValue = checkpanic multiMap.cloneWithType(EmployeeDoubleMap);
    assertMapValuePart1(mapValue);
    assertMapValuePart2(mapValue);
    test:assertEquals(mapValue.length(), 2);
    test:assertEquals((typeof mapValue).toString(), "typedesc EmployeeDoubleMap");
}

function assertMapValuePart1(map<map<Employee>> mapValue) {
    test:assertEquals(mapValue["aa"]["a"]["name"], "Raja");
    test:assertEquals(mapValue["aa"]["a"]["age"], 25);
    test:assertEquals(mapValue["aa"]["a"]["status"], "single");
    test:assertEquals(mapValue["aa"]["a"]["batch"], "LK2014");
    test:assertEquals(mapValue["aa"]["a"]["school"], "Hindu College");
    test:assertEquals(mapValue["aa"]["b"]["name"], "Mohan");
    test:assertEquals(mapValue["aa"]["b"]["age"], 30);
    test:assertEquals(mapValue["aa"]["b"]["status"], "single");
    test:assertEquals(mapValue["aa"]["b"]["batch"], "LK2014");
    test:assertEquals(mapValue["aa"]["b"]["school"], "Hindu College");
}

function assertMapValuePart2(map<map<Employee>> mapValue) {
    test:assertEquals(mapValue["bb"]["a"]["name"], "Raja");
    test:assertEquals(mapValue["bb"]["a"]["age"], 25);
    test:assertEquals(mapValue["bb"]["a"]["status"], "single");
    test:assertEquals(mapValue["bb"]["a"]["batch"], "LK2014");
    test:assertEquals(mapValue["bb"]["a"]["school"], "Hindu College");
    test:assertEquals(mapValue["bb"]["b"]["name"], "Mohan");
    test:assertEquals(mapValue["bb"]["b"]["age"], 30);
    test:assertEquals(mapValue["bb"]["b"]["status"], "single");
    test:assertEquals(mapValue["bb"]["b"]["batch"], "LK2014");
    test:assertEquals(mapValue["bb"]["b"]["school"], "Hindu College");
}

function stampAnydataToIntMultiDimensionMap() returns map<map<map<int>>>|error {
    map<map<map<anydata>>> m = {
        "a": {"aa": {"aa": 11, "bb": 22}, "bb": {"aa": 11, "bb": 22}},
        "b": {
            "aaa": {
                "aa": 11,
                "bb": 22
            },
            "bbb": {"aa": 11, "bb": 22}
        }
    };
    map<map<map<int>>>|error mapValue = m.cloneWithType(IntTrippleMap);
    return mapValue;
}

function stampIntToAnydataMultiDimensionMap() returns map<map<map<anydata>>>|error {
    map<map<map<int>>> m = {
        "a": {"aa": {"aa": 11, "bb": 22}, "bb": {"aa": 11, "bb": 22}},
        "b": {
            "aaa": {
                "aa": 11,
                "bb": 22
            },
            "bbb": {"aa": 11, "bb": 22}
        }
    };
    map<map<map<anydata>>>|error mapValue = m.cloneWithType(AnydataTrippleMap);
    return mapValue;
}

function stampConstraintMapToAnydata() returns anydata|error {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<Teacher> teacherMap = {"a": p1, "b": p2};
    anydata|error anydataValue = teacherMap.cloneWithType(anydata);
    return anydataValue;
}

function stampConstraintMapToUnion() returns map<Teacher>|xml|error {
    Teacher p1 = {name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College"};
    Teacher p2 = {name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College"};
    map<Teacher> teacherMap = {"a": p1, "b": p2};
    map<Teacher>|xml|error anydataValue = teacherMap.cloneWithType(MapOrXMLTypeDesc);
    return anydataValue;
}

//---------------------------------- Negative Test cases -----------------------------------------------------------

type EmployeeClosedRecord record {|
    string name;
    string status;
    string batch;
|};

function stampMapToRecordNegative() returns EmployeeClosedRecord|error {
    map<string> m = {name: "Raja", status: "single", batch: "LK2014", school: "Hindu College"};
    EmployeeClosedRecord|error employee = m.cloneWithType(EmployeeClosedRecord);
    return employee;
}
