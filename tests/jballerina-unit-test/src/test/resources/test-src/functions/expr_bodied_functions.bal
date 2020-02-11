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

function sum(int a, int b) returns int => a + b;

function testBinaryExprs() {
    int s = sum(10, 20);
    assert(30, s);
}

function testClosures(int a) {
    int b = 20;
    var sum = function (int i, int j) returns int => i + j + a + b;
    int result = sum(50, 100);
    assert(a + b + 150, result);
}

type Person record {
    string fname;
    string lname;
    int age;
};

type Employee record {
    string name;
};

function toEmployee(Person p) returns Employee => {
    name: p.fname + " " + p.lname
};

function testRecordAsAnExpr() {
    Employee e = toEmployee({fname: "John", lname: "Doe", age: 25});
    assert("name=John Doe", e.toString());
}

function getSameRef(Person p) returns Person => p;

function testSameVarRefAsExpr() {
    Person p1 = {fname: "John", lname: "Doe", age: 25};
    Person p2 = getSameRef(p1);
}

// Util functions

function assert(anydata expected, anydata actual) {
    if (expected != actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}

function assertSameRef(anydata expected, anydata actual) {
    if (expected !== actual) {
        typedesc<anydata> expT = typeof expected;
        typedesc<anydata> actT = typeof actual;
        string reason = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
        error e = error(reason);
        panic e;
    }
}


