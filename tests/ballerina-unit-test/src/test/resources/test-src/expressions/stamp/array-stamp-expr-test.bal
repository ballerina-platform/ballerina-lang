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

type Student record {
    string name;
    string status;
    string batch;
    string school;
    !...
};

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


function stampRecordToAnydataArray() returns anydata[] {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata[] anyArray = anydata[].stamp(teacherArray);

    return anyArray;
}

function stampAnydataToRecordArray() returns Teacher[]|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    anydata[] anydataArray = [p1, p2];
    Teacher[]|error teacherArray = Teacher[].stamp(anydataArray);

    return teacherArray;
}

function stampAnydataToSimilarOpenRecordArray() returns Employee[]|error  {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    anydata[] teacherArray = [p1, p2];
    Employee[]|error  employeeArray = Employee[].stamp(teacherArray);

    return employeeArray;
}

function stampRecordToSimilarOpenRecordArray() returns Employee[] {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    Employee[] employeeArray = Employee[].stamp(teacherArray);

    return employeeArray;
}

function stampConstraintArrayToJSONArray() returns json|error {
    Student [] studentArray = [{ name: "John", status: "single", batch: "LK2014", school: "Hindu College" },
    { name: "Raja", status: "married", batch: "LK2014", school: "Hindu College" }];

    json|error  jsonArray = json.stamp(studentArray);

    return jsonArray;
}

function stampRecordToAnydata() returns anydata {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata anydataArray = anydata.stamp(teacherArray);

    return anydataArray;
}

function stampRecordToAnydataArrayV2() returns anydata[] {
    Teacher p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Teacher p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Teacher[] teacherArray = [p1, p2];
    anydata[] anydataArray = anydata[].stamp(teacherArray);

    return anydataArray;
}

function stampAnydataArrayToUnion() returns Employee[]|int|error  {
    Employee p1 = { name: "Raja", age: 25, status: "single", batch: "LK2014", school: "Hindu College" };
    Employee p2 = { name: "Mohan", age: 30, status: "single", batch: "LK2014", school: "Hindu College" };

    Employee[] teacherArray = [p1, p2];
    Employee[]|int|error  employeeArray = Employee[]|int.stamp(teacherArray);

    return employeeArray;
}

function stampArrayValueToTuple() returns (Employee, Student)|error {
    Employee[] arrayValue = [{ name: "Mohan", status: "single", batch: "LK2015", school: "Royal College" },
    { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" }];

    (Employee, Student)|error returnValue = (Employee, Student).stamp(arrayValue);
    return returnValue;
}

