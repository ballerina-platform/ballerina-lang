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

import ballerina/lang.value;

annotation annot;

const PI = 3.14;

enum Colour {
    RED, GREEN, BLUE
}

type Foo int;

class PersonClass {
    string name;

    function getName() returns string => self.name;
}

xmlns "http://ballerina.io" as b7a;

function test(string p) {
    anydata ad;
    any a;
    int[] arr;
    boolean b;
    byte byt;
    decimal d;
    error e;
    float f;
    function fn;
    future fu;
    handle h;
    int[] & readonly intArr;
    int:Signed8 si8;
    int:Signed16 si16;
    int:Signed32 si32;
    int i;
    int:Unsigned8 usi8;
    int:Unsigned16 usi16;
    int:Unsigned32 usi32;
    json j;
    map<string> m;
    never n;
    () nil;
    object {
        string name;
        function getName() returns string;
    } o;
    readonly r;
    record {|string name;|} rec;
    10 s;
    stream<int> st;
    string:Char c;
    string s;
    table<record{string name;}> t;
    [string, int] tup;
    typedesc<int> td;
    Foo foo;
    int|string u;
    xml:Comment xc;
    xml:Element xe;
    xml:ProcessingInstruction xpi;
    xml:Text xt;
    xml x;
}

function workers() {
    worker w {
    }
}

service HelloWorld "GreetingService" on new Listener() {

    resource function get greet/[int x]/hello/[float y]/[string... rest] () returns json => { output: self.greeting };
}

public class Listener {
    public function start() returns error? {
    }

    public function gracefulStop() returns error? {
    }

    public function immediateStop() returns error? {
    }

    public function detach(service object {} s) returns error? {
    }

    public function attach(service object {} s, string[]? name = ()) returns error? {
    }
}
