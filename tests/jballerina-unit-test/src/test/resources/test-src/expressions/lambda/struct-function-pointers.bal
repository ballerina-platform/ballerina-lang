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

type Person record {
    string fname;
    string lname;
    function (string, string) returns (string) getName?;
    function (Person p) returns (string) getPersonName?;
};

function getFullName (string f, string l) returns (string) {
    return l + ", " + f;
}

function test1() returns [string, string]{
    Person bob = {fname:"bob", lname:"white", getName: function (string fname, string lname) returns (string){
                                                              return fname + " " + lname;
                                                          }};
    Person tom = {fname:"tom", lname:"smith", getName: getFullName};

    string x = bob.getName(bob.fname, bob.lname );
    string y = tom.getName(tom.fname, tom.lname );
    return [x, y];
}

function test2() returns (string){
    Person bob = {fname:"bob", lname:"white"};

    string x = bob.getName(bob.fname, bob.lname );
    return x;
}

function getPersonNameFullName (Person p) returns (string){
    return p.lname + ", " + p.fname;
}

function test3() returns (string){
    Person bob = {fname:"bob", lname:"white", getPersonName : getPersonNameFullName};
    string x = bob.getPersonName(bob);
    return x;
}

class Context {
    function getJsonVal(anydata a) returns json|error {
        json j = {j: a.toString()};
        return j;
    }
}

type FunctionType function (Context, anydata) returns json|error;
type FunctionEntry [FunctionType, typedesc<anydata>];
map<FunctionEntry> functions = { };

function testClassTypeAsParamtype() {
    FunctionType func = (c, d) => c.getJsonVal(d);
    functions["func"] = [func, int];
    var f = <[FunctionType, typedesc<anydata>]> functions["func"];
    FunctionType f1 = f[0];
    json|error j = f1(new(), 1);
    var k = <map<json>>j;
    string q = <string> k["j"];
    assertEquality("1", q);
}

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError("Assertion Error",
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

type AssertionError error;
