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

function arrayLengthAccessTestAssignmentCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arr.length();
    return length;
}

function arrayLengthAccessTestFunctionInvocationCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length;
    length = arrayLength(arr.length());
    return length;
}

function arrayLength (int x) returns (int) {
    return x;
}

function arrayLengthAccessTestVariableDefinitionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = arrayLength(arr.length());
    return length;
}

function arrayLengthAccessTestArrayInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] tempArr = [arr.length(),(x+y)];
    return tempArr[0];
}

function arrayLengthAccessTestMapInitializerCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    map tempMap = {"length": arr.length()};
    int length;
    length =check <int> tempMap.length;
    return length;
}

function arrayLengthAccessTestReturnStatementCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    return arr.length();
}

function arrayLengthAccessTestMultiReturnStatementCase (int x, int y) returns (int,int,int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int[] brr = [];
    brr[0] = 1;
    int[] crr = [];
    crr[0] = 1;
    crr[1] = x + y;
    return (arr.length(), brr.length(), crr.length());
}

function arrayLengthAccessTestTypeCastExpressionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    int length = <int> arr.length();
    return length;
}

function arrayLengthAccessTestIfConditionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if (arr.length() == 3) {
        return 3;
    } else{
        return 0;
    }
}

function arrayLengthAccessTestBinaryExpressionCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    if (arr.length() == arr.length()) {
        return 3;
    } else {
        return 0;
    }
}

function arrayLengthAccessTestStructFieldAccessCase (int x, int y) returns (int) {
    int[] arr = [];
    arr[0] = x;
    arr[1] = y;
    arr[2] = arr[0] + arr[1];
    Person jack = {name:"Jack", days:arr};

    if (jack.days.length() == 3) {
        return 3;
    } else {
        return 0;
    }
}

type Person record {
    string name;
    int[] days;
};

type Employee record {
    int id;
    string name;
    float salary;
};

type Empty record {};

function arrayLengthAccessTestJSONArrayCase (int x, int y) returns (int) {
    json arr = [x,y,5,5,6,6];
    int length;
    length = arr.length();
    return length;
}

function lengthOfMap () returns (int) {
    map namesMap = {fname:"foo", lname:"bar", sname:"abc", tname:"pqr"};
    int length = namesMap.length();
    return length;
}

function lengthOfEmptyMap () returns (int) {
    map namesMap;
    int length = namesMap.length();
    return length;
}

function lengthOfSingleXmlElement() returns (int) {
    xml x1 = xml `<book>The Lost World</book>`;
    int length = x1.length();
    return length;
}

function lengthOfMultipleXmlElements() returns (int) {
    xml x1 = xml `<book>The Lost World</book>`;
    xml x2 = xml `Hello, world!`;
    xml x3 = xml `<!--I am a comment-->`;
    xml x4 = xml `<?target data?>`;
    xml x5 = x1 + x2 + x3 + x4;
    int length = x5.length();
    return length;
}

function lengthOfTuple() returns (int) {
    (int, string) a = (10, "John");
    int length = a.length();
    return length;
}

function lengthOfTable() returns (int) {
    table<Employee> tbEmployee = table {
        { key id, name, salary },
        [ { 1, "Mary",  300.5 },
        { 2, "John",  200.5 },
        { 3, "Jim", 330.5 }
        ]
    };
    int length = tbEmployee.length();
    return length;
}

function lengthOfEmptyTable() returns (int) {
    table<Employee> tbEmployee = table {
        { key id, name, salary }
    };
    int length = tbEmployee.length();
    return length;
}

function lengthOfRecord() returns (int) {
    Employee emp = {id : 1 , name : "John", salary: 300};
    int length = emp.length();
    return length;
}

function lengthOfEmptyRecord() returns (int) {
    Empty emp = {};
    int length = emp.length();
    return length;
}

function accessLengthOfNullArray() returns (int) {
    int[] arr;
    int length = arr.length();
    return length;
}

function accessLengthOfNullMap() returns (int) {
    map m;
    int length = m.length();
    return length;
}

function accessLengthOfNullJson() returns (int) {
    json j;
    int length = j.length();
    return length;
}

function accessLengthOfNullTuple() returns (int) {
    (int, int )a;
    int length = a.length();
    return length;
}

function accessLengthOfNullXML() returns (int) {
    xml x;
    int length = x.length();
    return length;
}
