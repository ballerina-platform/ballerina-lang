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
};

type Employee record {
    string name;
    boolean intern;
};

Person person1 = { name: "John" };
Employee employee1 = { name: "John", intern: true };

function basicTupleAssignment() returns string {
    [int, string, boolean...] t = [1, "s", true, true];
    [int, string, boolean...] t1 = [2, "s", true];
    [int, string, boolean...] t2 = [3, "s"];
    return t.toString() + " " + t1.toString() + " " + t2.toString();
}

function tupleAssignmentWithNilRestDescriptor() returns string {
    [int, string, ()...] t = [1, "s"];
    [int, string, ()...] t1 = [1, "s", ()];
    [int, string, ()...] t2 = [1, "s", (), ()];
    [(()|())...] t3 = [(), ()];
    return t.toString() + " " + t1.toString() + " " + t2.toString() + " " + t3.toString();
}

function tupleAssignmentWithOnlyRestDescriptor() returns string {
    [int...] t = [1, 2];
    [string...] t1 = ["s", "s"];
    [()...] t2 = [(), ()];
    return t.toString() + " " + t1.toString() + " " + t2.toString();
}

function tupleCovarianceWithRestDescriptor() returns string {
    [string, Employee...] e = ["s", employee1, employee1, employee1];
    [string, Person...] t = e;
    string s = "";
    foreach var i in t {
        s += i.toString() + " ";
    }
    return s;
}

function tupleCovarianceWithOnlyRestDescriptor() returns string {
    [Employee...] e = [employee1, employee1, employee1];
    [Person...] t = e;
    string s = "";
    foreach var i in t {
        s += i.toString() + " ";
    }
    return s;
}

function testFunctionInvocation() returns string {
    [string, float, boolean...] t = ["y", 5.0, true, false];
    string x = testTuples(t);
    return x;
}

function testTuples([string, float, boolean...] t) returns string {
    string s = "";
    foreach var i in t {
        s += i.toString() + " ";
    }
    return s;
}

function testFunctionReturnValue() returns string {
    [string, float, boolean...] t = testReturnTuples("x");
    string s = "";
    foreach var i in t {
        s += i.toString() + " ";
    }
    return s;
}

function testReturnTuples(string a) returns [string, float, boolean...] {
    [string, float, boolean...] t = [a, 5.0, true, false];
    return t;
}

function testIndexBasedAccess() returns string {
    [boolean, string...] t = [true, "a", "b", "c"];
    boolean tempBool = t[0];
    string tempStringA = t[1];
    string tempStringB = t[2];
    string tempStringC = t[3];
    t[0] = false;
    t[1] = "a1";
    t[2] = "b1";
    t[3] = "c1";
    string s = "";
    foreach var i in t {
        s += i.toString() + " ";
    }
    return s;
}

function testIndexBasedAccessNegative() returns string {
    [boolean, string...] t = [true, "a", "b", "c"];
    string tempStringC = t[4];
    string s = "";
    foreach var i in t {
        s += i.toString();
    }
    return s;
}
