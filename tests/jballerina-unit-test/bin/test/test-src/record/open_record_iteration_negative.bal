// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

type Person record {
    string name;
    int age;
    Address address;
};

type Address record {
    string street;
    string city;
};

Person p = { name: "John Doe", age: 25, address: { street: "Palm Grove", city: "Colombo 3" }, "profession": "Software Engineer" };

function testInvalidArgForForeachWithOpenRecords() {
    any[] vals = [];
    int i = 0;

    foreach var [k, val, e] in p {
        vals[i] = val;
        i += 1;
    }
}

function testInvalidForeachOpWithOpenRecords() {
    p.forEach(function ([string, any] entry) {
    });

    p.forEach(function (string|int|Address entry) {
    });

    p.forEach(function (any entry) {
    });
}

function testInvalidMapOpWithOpenRecords() {
    map<any> newp = p.'map(function ([string, any] entry) returns [string, any] {
        return ["", ""];
    });

    newp = p.'map(function (anydata entry) returns any {
        return "";
    });

    newp = p.'map(function (anydata entry) returns any|error {
        return "";
    });

    newp = p.'map(function (anydata entry) returns string|int|Address {
        return "";
    });

    Person invMap = p.'map(function (anydata entry) returns anydata {
        return "";
    });
}

function testInvalidFilterOpWithOpenRecords() {
    map<any> newp = p.filter(function ([string, any] entry) returns boolean {
        return true;
    });

    newp = p.filter(function (anydata entry) returns boolean {
        return true;
    });

    newp = p.filter(function (anydata entry) returns string {
        return "";
    });

    newp = p.filter(function (anydata entry) returns [string, any, string] {
        return ["", "", ""];
    });

    Person invFil = p.filter(function (anydata entry) returns boolean {
        return false;
    });
}


type RestrictedGrades record {|
    int maths;
    int physics;
    int chemistry;
    int...;
|};


function testInvalidChainedItrOpReturns() {
    RestrictedGrades f = {maths: 80, physics: 75, chemistry: 65, "english": 78};

    map<int> m = f.'map(function (int grade) returns int {
        return grade + 10;
    })
    .'map(function (int g) returns string {
        if (g > 75) {
            return "PASS";
        }
        return "FAIL";
    })
    .filter(function (string status) returns boolean {
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .'map(function (string status) returns float {
        if (status == "PASS") {
            return 4.2;
        }
        return 0.0;
    });
}

function testInvalidChainedItrOpReturns2() {
    RestrictedGrades f = {maths: 80, physics: 75, chemistry: 65, "english": 78};

    int[] ar = f.'map(function (int grade) returns int {
        return grade + 10;
    })
    .'map(function (int grade) returns string {
        if (grade > 75) {
            return "PASS";
        }
        return "FAIL";
    })
    .filter(function (string status) returns boolean {
        if (status == "PASS") {
            return true;
        }
        return false;
    })
    .'map(function (string status) returns float {
        if (status == "PASS") {
            return 4.2;
        }
        return 0.0;
    });
}
