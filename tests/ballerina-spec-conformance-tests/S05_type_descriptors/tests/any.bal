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

import ballerina/test;

int a = 1;
string b = "test string 1";
float c = 2.0;
decimal d = 3.0;
boolean e = true;
() f = ();

(int|boolean)[2] g = [a, e];
(string, float, decimal) h = (b, c, d);

map<string|FooRecord|FooObject> i = {
    one: j,
    two: b,
    three: p
};
FooRecord j = { fooFieldOne: b };
table<BazRecord> k = table{};
xml l = xml `<book>The Lost World</book>`;
error errValOne = error("error reason one");
error errValTwo = error("error reason two");
map<error> m = { 
    one: errValOne ,
    two: errValTwo
};

function (int x) returns int n = getIncrementedInt;
future<int> o = start getIncrementedInt(a);
FooObject p = new(b);
service q = service {
    resource function mockResource() {
    }
};
typedesc r = json;

type s 1;
float|boolean t = e;
string? u = f;
any v = b;
anydata w = d;
byte x = 1;
json y = e;

stream<boolean> z = new;

function getIncrementedInt(int iVal) returns int {
    return iVal + 1;
}

// any-type-descriptor := any

// The type descriptor any describes the type consisting of all values other than errors. 
// A value belongs to the any type if and only if its basic type is not error. 
// Thus all values belong to the type any|error.
// Note that a structure with members that are errors belongs to the any type.
@test:Config {
    dataProvider: "anyValueDataProvider"
}
function testAnyTypeDescriptor(any av) {
    any av2 = av;
    test:assertEquals(av2, av, msg = "expected variable to hold the assigned value");
}

function anyValueDataProvider() returns any[][] {
    return [
        [a],
        [b],
        [c],
        [d],
        [e],
        [f],
        [g],
        [h],
        [i],
        [j],
        [k],
        [l],
        [m],
        [n],
        [o],
        [p],
        [q],
        [r],
        [s],
        [t],
        [u],
        [v],
        [w],
        [x],
        [y],
        [z]
    ];
}

public type FooRecord record {|
    string fooFieldOne;
|};

public type BazRecord record {
    float bazFieldOne;
};

public type FooObject object {
    public string fooFieldOne;

    public function __init(string fooFieldOne) {
        self.fooFieldOne = fooFieldOne;
    }

    public function getFooFieldOne() returns string {
        return self.fooFieldOne;
    }
};
