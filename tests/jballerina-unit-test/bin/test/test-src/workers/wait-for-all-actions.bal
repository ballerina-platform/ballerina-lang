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

import ballerina/jballerina.java;

function waitTest1() returns map<anydata|error> { // {f1: 7, f2: 22, f4: "hello foo", f6: true}
    future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
    future<int> f2 = @strand{thread:"any"} start add_2(10, 12);
    future<string> f3 = @strand{thread:"any"} start concat("foo");
    future<boolean> f4 = @strand{thread:"any"} start status();

    map<anydata|error> result = wait {f1, f2, f3, f4};
    return result;
}

function waitTest2() returns map<int|string|error> { // {f1: 7, str1: "hello foo", f3: 150, str2: "hello xyz"}
    future<int> f1 = start add_1(5, 2);
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    future<int> f3 = @strand{thread:"any"} start add_3(50, 100);
    future<int|string> f7 =  start concat("xyz");

    map<int|string|error> result = wait {f1, str1: f4, f3: f3, str2: f7};
    return result;
}

function waitTest3() returns record {| int|error f1; int|error f3; string|error f5; |} { // {f1: 7, f3: 150, f5: "hello bar"}
    future<int> f1 = start add_1(5, 2);
    future<int> f3 = @strand{thread:"any"} start add_3(50, 100);
    future<string> f5 = @strand{thread:"any"} start concat("bar");

    record {| int|error f1; int|error f3; string|error f5; |} result = wait {f1, f3, f5};
    return result;
}

function waitTest4() returns map<anydata|error> { // {f1: 7, f2: 22, f4: "hello foo"}
    future<int> f1 = start add_1(5, 2);
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    future<int> f2 = @strand{thread:"any"} start add_2(10, 12);
    record { int|error f1 = 0; int|error f2 = 0; string|error f4 = "";} anonRec = wait {f1, f4, f2};

    map<anydata|error> m = {};
    m["f1"] = anonRec.f1;
    m["f2"] = anonRec.f2;
    m["f4"] = anonRec.f4;
    return m;
}

function waitTest5() returns map<anydata|error> { // {id: 66, name: "hello foo"}
    future<string> f5 = @strand{thread:"any"} start concat("foo");
    record { int|error id; string|error name; } anonRec = wait {id: fuInt(), name: f5};

    map<anydata|error> m = {};
    m["id"] = anonRec.id;
    m["name"] = anonRec.name;
    return m;
}

function waitTest6() returns map<anydata|error> { // {idField: 150, stringField: "hello foo"}
    future<int> f3 = @strand{thread:"any"} start add_3(50, 100);
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    record { int|error idField; string|error stringField;} anonRec = wait {idField: f3, stringField: f4};

    map<anydata|error> m = {};
    m["idField"] = anonRec.idField;
    m["stringField"] = anonRec.stringField;
    return m;
}

function waitTest7() returns int|error { // {f1: 7}
    future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
    record { int|error f1; string|error f3?; } anonRec = wait {f1};
    int|error result = anonRec.f1;
    return result;
}

function waitTest8() returns firstRec { // {id: 8, name: "hello moo"}
    future<int> f1 = @strand{thread:"any"} start add_1(4, 4);
    future<string> f2 = @strand{thread:"any"} start concat("moo");
    firstRec result = wait {id: f1, name: f2};
    return result;
}

function waitTest9() returns secondRec { // {f1: 20, f5: "hello foo"}
    future<int> f1 = @strand{thread:"any"} start add_1(10, 10);
    future<string> f5 = @strand{thread:"any"} start concat("foo");
    secondRec result = wait {f1, f5};
    return result;
}

function waitTest10() returns secondRec { // {f1: 30, f5: "hello xyz"}
    future<int> f1 = @strand{thread:"any"} start add_1(20, 10);
    future<string> f5 = @strand{thread:"any"} start concat("xyz");
    secondRec result = wait {f1: f1, f5};
    return result;
}

function waitTest11() returns thirdRec { // {f1: 30, f5: "hello bar"}
    future<int> f1 = @strand{thread:"any"} start add_1(20, 10);
    future<string> f2 = @strand{thread:"any"} start concat("bar");
    thirdRec result = wait {f1: f1, 'field: f2};
    return result;
}

function waitTest12() returns fourthRec { // {f1: 30, f5: "hello bar"}
    future<int> f1 = @strand{thread:"any"} start add_1(20, 66);
    fourthRec result = wait {id: f1};
    return result;
}

function waitTest13() returns fourthRec { // error
    future<int> f1 = @strand{thread:"any"} start add_panic(20, 66);
    fourthRec result = wait {id: f1};
    return result;
}


function waitTest14() returns map<anydata|error> { // {idField: 150, stringField: "hello foo"}
    future<string> f4 = @strand{thread:"any"} start concat("foo");
    future<int> f3 = @strand{thread:"any"} start add_panic(50, 100);
    record { int|error i; string|error j;} anonRec = wait {i: f3, j: f4};

    map<anydata|error> m = {};
    m["i"] = anonRec.i;
    m["j"] = anonRec.j;
    return m;
}

function waitTest15() returns map<anydata|error> { // {f1: 7, f2: 22, f4: "hello foo", f6: true}
    future<int> f1 = start add_1(5, 2);
    future<string> f3 = start concat("foo");
    future<boolean> f4 = start status();
    future<int> f2 = start add_panic(10, 12);

    map<anydata|error> result = wait {f1, f2, f3, f4};
    return result;
}

function waitTest16() returns int {
    future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
    future<string> f3 =  @strand{thread:"any"}start concat("foo");
    future<boolean> f4 = @strand{thread:"any"} start status();
    future<int> f2 = @strand{thread:"any"} start add_panic(10, 12);

    var result = trap wait {f1, f2, f3, f4};
    if (result is map<int|string|boolean>) {
        return 9;
    } else {
        return 0;
    }
}

function waitTest17() returns any {
    worker w1  returns any {
        future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
        future<int> f2 = @strand{thread:"any"} start add_3(50, 100);
        TwoIntOrErrorFields result = wait {f1, f2};
        return result;
    }
    worker w2 returns any {
        future<int> f1 = @strand{thread:"any"} start add_1(6, 6);
        future<string> f2 = @strand{thread:"any"} start concat("foo");
        map <int|string|error> m = wait {id: f1, name : f2};
        sleep(2000);
        return m;
    }
    record {| any f1; any f2; |} a = wait {f1: w1, f2: w2};
    return a;
}

function waitTest18() returns secondRec{
    secondRec r = {};
    worker w1  returns secondRec {
        future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
        future<string> f2 = @strand{thread:"any"} start concat("foo");
        secondRec result = wait {f1, f5: f2};
        return result;
    }
    r = wait w1;
    return r;
}

function waitTest19() returns map<int>{
    fork {
        worker w1 returns int{
            int x = 30;
            return x;
        }
        @strand{thread:"any"}
        worker w2 returns int{
            int y = 15;
            int g = y + 1;
            return g;
        }
    }
    map<int> results = wait {w1, w2};
    worker wy { }
    return results;
}

function waitTest20() returns map<int|string> {
    fork {
        @strand{thread:"any"}
        worker w1 returns int {
            string a = "hello world";
            a -> w2;
            int b = <- w2;
            return b;
        }
        @strand{thread:"any"}
        worker w2 returns string {
            string a = "no message";
            int b = 15;
            a = <- w1;
            b -> w1;
            return a;
        }
    }
    map<int|string> results = wait {w3: w1, w2};
    return results;
}

function waitTest21() returns sealedRec {
    future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
    future<string> f2 = @strand{thread:"any"} start concat("foo");

    sealedRec rec = wait {id: f1, name : f2};
    return rec;
}

function waitTest22() returns openRec {
    future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
    future<string> f2 = @strand{thread:"any"} start concat("foo");
    future<string> f3 = @strand{thread:"any"} start concat("bar");

    openRec rec = wait {id: f1, name : f2, status: f3};
    return rec;
}

function waitTest23() returns restRec1 {
    future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
    future<string> f2 = @strand{thread:"any"} start concat("foo");
    future<int> f3 = @strand{thread:"any"} start add_1(10, 10);

    restRec1 rec = wait {id: f1, name : f2, status: f3};
    return rec;
}

function waitTest24() returns restRec2 {
    future<int> f1 = @strand{thread:"any"} start add_1(10, 2);
    future<string> f2 = @strand{thread:"any"} start concat("foo");
    future<string> f3 = @strand{thread:"any"} start concat("bar");

    restRec2 rec = wait {id: f1, name : f2, status: f3};
    return rec;
}

function waitTest25() returns map<anydata|error> {
    future<int> f1 = @strand{thread:"any"} start add_1(5, 2);
    future<string> f2 = @strand{thread:"any"} start concat("foo");

    record {| int|error id = 0; string|error name = "default"; |} anonRec = wait {id: f1, name : f2};
    map<anydata|error> m = {};
    m["id"] = anonRec.id;
    m["name"] = anonRec.name;
    return m;
}

function waitTest26() returns map<anydata|error> {
    future<int> f1 = @strand{thread:"any"} start add_1(15, 15);
    future<string> f2 = @strand{thread:"any"} start concat("world");
    future<string> f3 = @strand{thread:"any"} start concat("moo");

    record {|
        int|error id = 0;
        string|error name = "default";
        anydata|error...;
    |} anonRec = wait {id: f1, name : f2, status: f3};
    map<anydata|error> m = {};
    m["id"] = anonRec.id;
    m["name"] = anonRec.name;
    m["status"] = anonRec["status"];
    return m;
}

function waitTest27() returns map<anydata|error> {
    future<int> f1 = @strand{thread:"any"} start add_1(100, 100);
    future<string> f2 = @strand{thread:"any"} start concat("mello");
    future<string> f3 = @strand{thread:"any"} start concat("sunshine");

    record {| int|error id = 0; string|error name = "default"; string|error...; |} anonRec = wait {id: f1, name : f2,
    greet: f3};
    map<anydata|error> m = {};
    m["id"] = anonRec.id;
    m["name"] = anonRec.name;
    m["greet"] = anonRec["greet"];
    return m;
}

type sealedRec record {|
    int|error id = 0;
    string|error name = "default";
|};

type openRec record {|
    int|error id = 0;
    string|error name = "default";
    anydata|error...;
|};

type restRec1 record {|
    int|error id = 0;
    string|error name = "default";
    int|error...;
|};

type restRec2 record {|
    int|error id = 0;
    string|error name = "default";
    string|error...;
|};

type firstRec record {
    int|error id = 1;
    string|error name = "first-default";
};

type secondRec record {
    int|error f1 = 1;
    string|error f5 = "second-default";
};

type thirdRec record {
    int|error f1 = 0;
    string|error 'field = "third-default";
    int f4?;
};

type fourthRec record {
    int|string|error id = 0;
};

type TwoIntOrErrorFields record {|
    int|error f1;
    int|error f2;
|};
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
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
