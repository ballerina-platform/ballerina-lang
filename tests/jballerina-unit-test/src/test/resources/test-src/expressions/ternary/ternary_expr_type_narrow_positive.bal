// Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

function testTernaryTypeNarrow() returns boolean {
    test1();
    test2({i: 2}, {j: "s"}, {i: "s"});

    return true;
}

function test1() {
    int|string? x = 1;
    _ = x is int ? intFn(x) : stringOptFn(x); // OK

    int|string|boolean y = 1;
    _ = y is string ? stringFn(y) : (y is int ? intFn(y) : booleanFn(y)); // OK
    _ = y !is string ? (y is int ? intFn(y) : booleanFn(y)) : stringFn(y); // OK

    true|false? z = true;
    _ = z == true ? trueFn(z) : (z == false ? falseFn(z) : nilFn(z)); // OK
    _ = !(z != true) ? trueFn(z) : (z != false ? nilFn(z) : falseFn(z)); // OK

    int? a = 3;
    _ = a != () ? a : nilFn(a); // OK
}

type A record {|
    int i;
|};

type B record {|
    string i;
|};

type C record {|
    string j;
|};

function aFn(A x) {
}

function bFn(B x) {
}

function cFn(C x) {
}

function abFn(A|B x) {
}

function test2(A|B x, A|C y, B|C z) {
    _ = x is A ? aFn(x) : abFn(x); // OK

    _ = y is A ? aFn(y) : cFn(y); // OK

    _ = z is B ? bFn(z) : cFn(z); // OK
}

function intFn(int x) {
}

function stringFn(string x) {
}

function stringOptFn(string? x) {
}

function booleanFn(boolean x) {
}

function trueFn(true x) {
}

function falseFn(false x) {
}

function nilFn(() x) {
}
