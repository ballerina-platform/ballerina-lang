// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

client "http://example.com/apis/one.yaml" as foo;

const foo:IntMap C1 = {a: 1}; // OK
public const foo:IntMap C2 = {a: 1}; // error

foo:IntMap v1 = {a: 1}; // OK
public foo:IntMap v2 = {a: 1}; // error

public function main() {
    foo:ClientConfiguration x; // OK
}

class Class1 { // OK
    foo:IntMap a;

    function init(foo:IntMap a) {
        self.a = a;
    }
}

public class Class2 { // error
    foo:IntMap a;

    function init(foo:IntMap a) {
        self.a = a;
    }
}

public class Class3 { // error
    public function fn() returns foo:ClientConfiguration => {'limit: 5}; // error
}

function fn(foo:ClientConfiguration x, foo:ClientConfiguration y = {'limit: 5}, foo:ClientConfiguration... z) returns foo:ClientConfiguration => x; // OK

public function fn2(foo:ClientConfiguration x, foo:ClientConfiguration y = {'limit: 5}, foo:ClientConfiguration... z) returns foo:ClientConfiguration => x; // errors

foo:IntMap[] v3 = []; // OK
public foo:IntMap[] v4 = []; // error

error<foo:IntMap> v5 = error("e1"); // OK
public error<foo:IntMap> v6 = error("e2"); // error

function (foo:IntMap) v7 = function (foo:IntMap a) { // OK

};

public function (foo:IntMap) v8 = function (foo:IntMap a) { // error

};

function (foo:IntMap a, foo:IntMap b = {}, foo:IntMap... c) returns foo:IntMap v9 =  // OK
function (foo:IntMap a, foo:IntMap b = {}, foo:IntMap... c) returns foo:IntMap {
    return a;
};

public function (foo:IntMap a, foo:IntMap b = {}, foo:IntMap... c) returns foo:IntMap v10 =  // error
function (foo:IntMap a, foo:IntMap b = {}, foo:IntMap... c) returns foo:IntMap {
    return a;
};

map<foo:IntMap> v11 = {}; // OK
public map<foo:IntMap> v12 = {}; // error

stream<foo:IntMap> v31 = new; // OK
public stream<foo:IntMap> v32 = new; // error

stream<foo:IntMap, error<foo:IntMap>?> v13 = new; // OK
public stream<int, error<foo:IntMap>?> v14 = new; // error

typedesc<foo:IntMap> v15 = foo:IntMap; // OK
public typedesc<foo:IntMap> v16 = foo:IntMap; // error

type T1 foo:IntMap; // OK
public type T2 foo:IntMap; // error

[foo:IntMap] v17 = [{}]; // OK
public [foo:IntMap] v18 = [{}]; // error

[foo:IntMap, foo:IntMap...] v19 = [{}]; // OK
public [foo:IntMap, foo:IntMap...] v20 = [{}]; // error

foo:IntMap|int v21 = 1; // OK
public foo:IntMap|int v22 = 1; // error

foo:IntMap & readonly v23 = {}; // OK
public foo:IntMap & readonly v24 = {}; // error

xml<foo:XmlElement> v25 = xml `<foo/>`; // OK
public xml<foo:XmlElement> v26 = xml `<foo/>`; // error

table<foo:IntMap> v27 = table []; // OK
public table<foo:IntMap> v28 = table []; // error

type T3 record {| // OK
    foo:IntMap a;
|};

public type T4 record {| // error
    foo:IntMap a;
    int b;
|};

type T5 object { // OK
    foo:IntMap a;
};

public type T6 object { // error
    foo:IntMap a;
};

public type T7 object { // error
    public function fn() returns foo:ClientConfiguration;
};

function testFn() returns future<foo:IntMap> {
    function () returns foo:IntMap f = () => {};
    future<foo:IntMap> ft = start f();
    return ft;
}

future<foo:IntMap> v29 = testFn(); // OK
public future<foo:IntMap> v30 = testFn(); // error
