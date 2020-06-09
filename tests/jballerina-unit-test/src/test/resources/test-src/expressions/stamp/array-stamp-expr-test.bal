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

type Teacher record {
    string name;
    int age;
    string status;
    string batch;
    string school;
};

//----------------------------Array Stamp -------------------------------------------------------------


function stampRecordToAnydataArray() returns anydata[]|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata[]|error anyArray = teacherArray.cloneWithType(anydata[]);

    return anyArray;
}

function stampAnydataToRecordArray() returns Teacher[]|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    anydata[] anydataArray = [p1, p2];
    Teacher[]|error teacherArray = anydataArray.cloneWithType(Teacher[]);

    return teacherArray;
}

function stampAnydataToSimilarOpenRecordArray() returns Employee[]|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    anydata[] teacherArray = [p1, p2];
    Employee[]|error  employeeArray = teacherArray.cloneWithType(Employee[]);

    return employeeArray;
}

function stampRecordToSimilarOpenRecordArray() returns Employee[]|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    Employee[]|error employeeArray = teacherArray.cloneWithType(Employee[]);

    return employeeArray;
}

function stampConstraintArrayToJSONArray() returns json|error {
    Student [] studentArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
    { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];

    json|error  jsonArray = studentArray.cloneWithType(json);

    return jsonArray;
}

function stampRecordToAnydata() returns anydata|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata|error anydataArray = teacherArray.cloneWithType(anydata);

    return anydataArray;
}

function stampRecordToAnydataArrayV2() returns anydata[]|error {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata[]|error anydataArray = teacherArray.cloneWithType(anydata[]);

    return anydataArray;
}

function stampAnydataArrayToUnion() returns Employee[]|int|error  {
    Employee p1 = { name: "Raja", "age": 25, status: "single", batch: "LK2014", "school": "Hindu College" };
    Employee p2 = { name: "Mohan", "age": 30, status: "single", batch: "LK2014", "school": "Hindu College" };

    Employee[] teacherArray = [p1, p2];
    Employee[]|int|error  employeeArray = teacherArray.cloneWithType(typedesc<Employee[]|int>);

    return employeeArray;
}

function stampArrayValueToTuple() returns [Employee, Student]|error {
    Employee[] arrayValue = [{ name: "Mohan", status: "single", batch: "LK2015", "school": "Royal College" },
    { name: "Raja", status: "single", batch: "LK2014", "school": "Hindu College" }];

    [Employee, Student]|error returnValue = arrayValue.cloneWithType([Employee, Student]);
    return returnValue;
}

//-------------------- Basic type array stamp ---------------------------------------------------------------

function stampJSONToBasicArray() returns int[]|error {
    json jsonValue = [1, 2, 3, 4];
    int[]|error returnArray = jsonValue.cloneWithType(int[]);

    return returnArray;
}

function stampAnydataToBasicArray() returns int[]|error {
    // todo
    anydata[] anydataValue = [1, 2, 3, 4];
    int[]|error returnArray = anydataValue.cloneWithType(int[]);

    return returnArray;
}

function stampAnydataArrayToBasicArray() returns int[]|error {
    anydata[] anydataArray = [1, 2, 3, 4];
    int[]|error returnArray = anydataArray.cloneWithType(int[]);

    return returnArray;
}

function stampJSONArrayToBasicArray() returns int[]|error {
    json[] jsonValue = [1, 2, 3, 4];
    int[]|error returnArray = jsonValue.cloneWithType(int[]);

    return returnArray;
}

function stampBasicArrayToJSON() returns json|error {
    int[] intArrayValue = [1, 2, 3, 4];
    json|error returnValue = intArrayValue.cloneWithType(json);

    return returnValue;
}

function stampBasicArrayToAnydata() returns anydata|error {
    int[] intArrayValue = [1, 2, 3, 4];
    anydata|error returnValue = intArrayValue.cloneWithType(anydata);

    return returnValue;
}

function stampBasicArrayToAnydataArray() returns anydata[]|error {
    int[] intArrayValue = [1, 2, 3, 4];
    anydata[]|error returnValue = intArrayValue.cloneWithType(anydata[]);

    return returnValue;
}

function stampBasicArrayToJSONArray() returns json[]|error {
    int[] intArrayValue = [1, 2, 3, 4];
    json[]|error returnValue = intArrayValue.cloneWithType(json[]);

    return returnValue;
}

function stampBasicArrayToTuple() returns [int,int]|error {
    int[] intArrayValue = [1, 2];
    [int,int]|error returnValue = intArrayValue.cloneWithType([int,int]);

    return returnValue;
}

function stampAnydataBasicArrayToTuple() returns [int,int]|error {
    int[] intArrayValue = [1, 2];
    anydata anydataValue = intArrayValue;
    [int,int]|error returnValue = anydataValue.cloneWithType([int,int]);

    return returnValue;
}

function stampBasicArrayToAnydataTuple() returns [anydata,anydata]|error {
    int[] intArrayValue = [1, 2];
    [anydata,anydata]|error returnValue = intArrayValue.cloneWithType([anydata,anydata]);

    return returnValue;
}

function stampBasicArrayToBasicArray() returns int[]|error {
    int[] intArrayValue = [1, 2];
    int[]|error returnValue = intArrayValue.cloneWithType(int[]);

    return returnValue;
}

function stampBasicMapArrayToAnydataMapArray() returns map<anydata>[]|error {
    map<int> map1 = {a: 5, b: 10};
    map<int> map2 = {a: 15, b: 20};

    map<int>[] intMap = [map1, map2];
    map<anydata>[]|error anydataMap = intMap.cloneWithType(map<anydata>[]);

    return anydataMap;
}

function stampRecordArrayToJsonArray() returns json[]|error {
    Employee e1 = { name: "Waruna", status: "single", batch: "LK2018", "age": 10 };
    Employee e2 = { name: "Heshitha", status: "single", batch: "LK2019", "age": 15 };
    Employee[] employeeArray = [e1, e2];
    json[] jsonArray = check employeeArray.cloneWithType(json[]);
    return jsonArray;
}
