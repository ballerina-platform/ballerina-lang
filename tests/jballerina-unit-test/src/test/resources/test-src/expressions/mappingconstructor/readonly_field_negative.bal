// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Employee record {|
    Details details;
    readonly string department;
|};

type Details record {|
    string name;
    int id;
|};

function testMutableValueForReadOnlyFieldForRecordCET() {
    Details details = {
        name: "May",
        id: 1234
    };

    Employee employee = {readonly details, department: "IT"};
    Employee|Details e2 = {department: "IT", readonly details: details};

    record {
        Employee employee;
        Employee e2;
    } rec = {
        readonly employee,
        readonly e2: {
            details: details,
            department: "IT"
        }
    };
}

function testMutableValueForReadOnlyFieldForMapCET() {
    Details details = {
        name: "May",
        id: 1234
    };

    map<Details|string> employeeMap = {readonly details, department: "IT"};
    map<string>|map<Details|string> e2 = {department: "IT", readonly details: details};

    record {
        map<Details|string> employeeMap;
        map<Details> e2;
    } rec = {
        readonly employeeMap,
        readonly e2: {
            details
        }
    };
}

type NonReadOnlyFields record {|
    future<int> x;
    any...;
|};

function testReadOnlyWithNeverReadOnlyFields() {
    future<int> x = start getInt();
    boolean[] bArr = [true, false];

    NonReadOnlyFields rec1 = {readonly x, readonly "y": bArr.toStream(), readonly "s": "str"};
    record {|future<any>...;|}|NonReadOnlyFields rec2 = {readonly x, readonly "y": bArr.toStream()};

    stream<boolean> y = bArr.toStream();
    map<any|error> mp1 = {readonly x: start getInt(), readonly y};
    map<any|error>|map<future<int>> mp2 = {i: 1, readonly x: start getInt(), readonly y};
}

function getInt() returns int => 1;
