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

function waitTest1() returns map<anydata> { // {f1: 7, f2: 22, f4: "hello foo", f6: true}
    future<int> f1 = start add_1(5, 2);
    future<int> f2 = start add_2(10, 12);
    future<string> f3 = start concat("foo");
    future<boolean> f4 = start status();

    map<anydata> result = wait {f1, f2, f3, f4};
    return result;
}

function waitTest2() returns map<int|string> { // {f1: 7, str1: "hello foo", f3: 150, str2: "hello xyz"}
    future<int> f1 = start add_1(5, 2);
    future<string> f4 = start concat("foo");
    future<int> f3 = start add_3(50, 100);
    future<int|string> f7 =  start concat("xyz");

    map<int|string> result = wait {f1, str1: f4, f3: f3, str2: f7};
    return result;
}

function waitTest3() returns any { // {f1: 7, f3: 150, f5: "hello bar"}
    future<int> f1 = start add_1(5, 2);
    future<int> f3 = start add_3(50, 100);
    future<string> f5 = start concat("bar");

    any result = wait {f1, f3, f5};
    return result;
}

function waitTest4() returns map<anydata> { // {f1: 7, f2: 22, f4: "hello foo"}
    future<int> f1 = start add_1(5, 2);
    future<string> f4 = start concat("foo");
    future<int> f2 = start add_2(10, 12);
    record { int f1 = 0; int f2 = 0; string f4 = "";} anonRec = wait {f1, f4, f2};

    map<anydata> m = {};
    m["f1"] = anonRec.f1;
    m["f2"] = anonRec.f2;
    m["f4"] = anonRec.f4;
    return m;
}

function waitTest5() returns map<anydata> { // {id: 66, name: "hello foo"}
    future<string> f5 = start concat("foo");
    record { int id; string name; } anonRec = wait {id: fuInt(), name: f5};

    map<anydata> m = {};
    m["id"] = anonRec.id;
    m["name"] = anonRec.name;
    return m;
}

function waitTest6() returns map<anydata> { // {idField: 150, stringField: "hello foo"}
    future<int> f3 = start add_3(50, 100);
    future<string> f4 = start concat("foo");
    record { int idField; string stringField;} anonRec = wait {idField: f3, stringField: f4};

    map<anydata> m = {};
    m["idField"] = anonRec.idField;
    m["stringField"] = anonRec.stringField;
    return m;
}

function waitTest7() returns int { // {f1: 7}
    future<int> f1 = start add_1(5, 2);
    record { int f1; string f3?; } anonRec = wait {f1};
    int result = anonRec.f1;
    return result;
}

function waitTest8() returns firstRec { // {id: 8, name: "hello moo"}
    future<int> f1 = start add_1(4, 4);
    future<string> f2 = start concat("moo");
    firstRec result = wait {id: f1, name: f2};
    return result;
}

function waitTest9() returns secondRec { // {f1: 20, f5: "hello foo"}
    future<int> f1 = start add_1(10, 10);
    future<string> f5 = start concat("foo");
    secondRec result = wait {f1, f5};
    return result;
}

function waitTest10() returns secondRec { // {f1: 30, f5: "hello xyz"}
    future<int> f1 = start add_1(20, 10);
    future<string> f5 = start concat("xyz");
    secondRec result = wait {f1: f1, f5};
    return result;
}

function waitTest11() returns thirdRec { // {f1: 30, f5: "hello bar"}
    future<int> f1 = start add_1(20, 10);
    future<string> f2 = start concat("bar");
    thirdRec result = wait {f1: f1, field: f2};
    return result;
}

function waitTest12() returns fourthRec { // {f1: 30, f5: "hello bar"}
    future<int> f1 = start add_1(20, 66);
    fourthRec result = wait {id: f1};
    return result;
}

function waitTest13() returns fourthRec { // error
    future<int> f1 = start add_panic(20, 66);
    fourthRec result = wait {id: f1};
    return result;
}


function waitTest14() returns map<anydata> { // {idField: 150, stringField: "hello foo"}
    future<string> f4 = start concat("foo");
    future<int> f3 = start add_panic(50, 100);
    record { int i; string j;} anonRec = wait {i: f3, j: f4};

    map<anydata> m = {};
    m["i"] = anonRec.i;
    m["j"] = anonRec.j;
    return m;
}

function waitTest15() returns map<anydata> { // {f1: 7, f2: 22, f4: "hello foo", f6: true}
    future<int> f1 = start add_1(5, 2);
    future<string> f3 = start concat("foo");
    future<boolean> f4 = start status();
    future<int> f2 = start add_panic(10, 12);

    map<anydata> result = wait {f1, f2, f3, f4};
    return result;
}

function waitTest16() returns int {
    future<int> f1 = start add_1(5, 2);
    future<string> f3 = start concat("foo");
    future<boolean> f4 = start status();
    future<int> f2 = start add_panic(10, 12);

    var result = trap wait {f1, f2, f3, f4};
    if (result is map<int|string|boolean>) {
        return 9;
    } else if (result is error) {
        return 0;
    } else {
        return 1;
    }
}

type firstRec record {
    int id = 1;
    string name = "first-default";
};

type secondRec record {
    int f1 = 1;
    string f5 = "second-default";
};

type thirdRec record {
    int f1 = 0;
    string field = "third-default";
    int f4?;
};

type fourthRec record {
    int|string id = 0;
};

// Util functions

function add_1(int i, int j) returns int {
    int k = i + j;
    int l = 0;
    while (l < 9999999) {
        l = l + 1;
    }
    return k;
}

function add_2(int i, int j) returns int {
    int k = i + j;
    return k;
}

function add_3(int i, int j) returns int {
    int k = i + j;
    return k;
}

function concat(string name) returns string {
    return "hello " + name;
}

function status() returns boolean {
    return true;
}

function fuInt() returns future<int> {
    future <int> i = start add_3(11, 55);
    return i;
}

function getEmpMap() returns map<string> {
    map<string> empMap = { fname: "foo", lname: "bar"};
    return empMap;
}

function getAddrMap() returns map<string> {
    map<string> addrMap = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03"};
    return addrMap;
}
function add_panic(int i, int j) returns int {
    int k = i + j;
    int l = 0;
    while (l < 9999999) {
        l = l + 1;
    }
    if (true) {
        error err = error("err from panic" );
        panic err;
    }
    return k;
}
