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

function testCastWithUnassignableTypes() {
    Def def = { name: "Em", id: 123.4 };
    Abc abc = <Abc> def;
}

function testCastForCurrentlyUnsupportedTypes() {
    future<int> f1 = start testFutureFunc();
    any a = f1;
    future<int> f2 = <future<int>> a;
}

function testInvalidCastToFiniteType() {
    boolean b = true;
    FooInt f = <FooInt> b;
}

function testInvalidCastFromFiniteType() {
    FooInt f = "foo";
    xml x = <xml> f;
}

type Abc record {
    string name;
    int id;
};

type Def record {
    json name;
    float id;
};

type Employee record {|
    string name;
|};

function testFutureFunc() returns int {
    return 1;
}

type FooInt "foo"|int;

function testCastWithErrorType() {
    json j = {
        name : "Name",
        address : {
            country : "Country",
            city : "City"
        }
    };

    var a = <int>foo();
    var b = <string>j.id;
    var c = <string>j.address.town;
}

function foo() returns int|error {
    return 1;
}

function testTypeCastInConstructorMemberWithUnionCETNegative() {
    string[]|int val1 = [];
    record {byte[] a;} a = {a: <byte[]> val1};
    record {byte[] a;}|record {string a;} b = {a: <byte[]> val1};

    any[]|error val2 = [];
    record {string[] a;}|record {string a;} c = {a: <byte[]> checkpanic val2};
}
