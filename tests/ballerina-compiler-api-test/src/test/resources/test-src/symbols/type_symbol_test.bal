// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function simpleTypes() {
    int a;
    boolean b;
    float c;
    decimal d;
    () e;
    string s;
    int[] arr;
}

function constrainedTypes() {
    xml x1;
    xml<xml> x2;
    xml<'xml:Element> x3;
    map<string> m;
    typedesc<anydata> td1;
    typedesc td2;
    table<Person> t1;
    table<Person> key<int> t2;
    future<string> f1;
    future f2;
}

function structuredTypes() {
    int[] ar1;
    int[TEN] ar2;
    [int, string] tup;
}

function behaviouralTypes() {
    error<ErrorData> err;
    handle h;
    stream<Person, error> st;
}

function otherTypes() {
    10 a;
    FOO b;
    any c;
    never d;
    readonly e;
    int|string f;
    Person & readonly g;
    int? h;
    anydata i;
    json j;
    byte k;
    distinct error err;
}

// utils
const FOO = "foo";
const TEN = 10;

type Person record {|
    int id;
    string name;
    int age;
|};

type ErrorData record {|
    string message;
    error cause?;
|};
