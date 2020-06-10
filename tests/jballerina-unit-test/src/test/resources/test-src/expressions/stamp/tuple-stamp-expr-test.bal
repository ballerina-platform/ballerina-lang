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

//-----------------------Tuple Type Stamp -------------------------------------------------------------------

function stampTupleValueV1() returns [string, Teacher]|error {
    [string, Teacher] tupleValue = ["Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" }];

    [string, Teacher]|error returnValue = tupleValue.cloneWithType([string, Teacher]);
    return returnValue;
}

function stampTupleValueV2() returns [string, Employee]|error {
    [string, Teacher] tupleValue = ["Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" }];

    [string, Employee]|error returnValue = tupleValue.cloneWithType([string, Employee]);
    return returnValue;
}

function stampTupleToAnydata() returns anydata|error {
    [string, Teacher] tupleValue = ["Mohan", { name: "Raja", age: 25, status: "single", batch: "LK2014", school:
    "Hindu College" }];

    anydata|error anydataValue = tupleValue.cloneWithType(anydata);
    return anydataValue;
}

function stampTupleValueToArray() returns Employee[]|error {
    [Employee, Person] tupleValue = [   { name: "Mohan", status: "single", batch: "LK2015", "school": "Royal College" },
                                        { name: "Raja", status: "single", batch: "LK2014", school: "Hindu College" }];

    Employee[]|error returnValue = tupleValue.cloneWithType(Employee[]);
    return returnValue;
}

function stampTupleToBasicArray() returns int[]|error {
    [int,int] intArray = [1, 2];
    int[]|error returnValue = intArray.cloneWithType(int[]);

    return returnValue;
}

function stampTupleToAnydataTuple() returns [anydata, anydata]|error {
    [int,int] intArray = [1, 2];
    [anydata, anydata]|error returnValue = intArray.cloneWithType([anydata, anydata]);

    return returnValue;
}

function stampAnydataTupleToBasicTypeTuple() returns [int, int]|error {
    [anydata,anydata] intArray = [1, 2];
    [int, int]|error returnValue = intArray.cloneWithType([int, int]);

    return returnValue;
}
