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

type Person record {|
    string fname = "";
    string lname = "";
    (function (string, string) returns string)? getName = ();
|};

function testNilableFuncPtrInvocation() {
    Person bob = {fname:"bob", lname:"white"};
    bob.getName = function (string fname, string lname) returns string {
        return fname + " " + lname;
    };
    string? x = bob.getName(bob.fname, bob.lname);
}

function testNilableFuncPtrInvocation2() {
    Person bob = {fname:"bob", lname:"white"};
    string? x = bob.getName(bob.fname, bob.lname);
}

type PersonB record {|
    string fname = "";
    string lname = "";
    (function (string, string) returns string)? getName = ();
|};

function testNilableFuncPtrInvocationInObj() {
    PersonB bob = {fname:"Bob", lname:"White"};
    bob.getName = function (string fname, string lname) returns string {
        return fname + " " + lname;
    };
    string? x = bob.getName(bob.fname, bob.lname);
}

function testNilableFuncPtrInvocationInObj2() {
    PersonB bob = {fname:"Bob", lname:"White"};
    string? x = bob.getName(bob.fname, bob.lname);
}
