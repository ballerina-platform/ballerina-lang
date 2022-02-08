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

function test2() {
    boolean x = true;
    _ = x is true ? trueFn(x) : falseFn(x); // Type not narrowed. issue #30598, #33217
    _ = x == true ? trueFn(x) : falseFn(x); // Type not narrowed. issue #30598, #33217
    _ = x != false ? trueFn(x) : falseFn(x); // Type not narrowed. issue #30598, #33217

    boolean? y = true;
    _ = y is true ? trueFn(y) : (y == false ? falseFn(y) : nilFn(y)); // Type not narrowed. issue #30598, #33217
}

function test3() {
    int|string|boolean x = "s";
    if x is string {

    } else {
        _ = x is int ? intFn(x) : booleanFn(x); // OK
    }

    while x !is string {
        _ = x is int ? intFn(x) : booleanFn(x); // OK
    }

    int|string|boolean|decimal y = "s";
    if y is string {

    } else {
        while y !is decimal {
            _ = y is int ? intFn(y) : booleanFn(y); // OK

        }
    }

    while y !is string {
        if !(y is decimal) {
            _ = y is int ? intFn(y) : booleanFn(y); // OK

        }
    }

    while y !is string {
        while y !is decimal {
            _ = y is int ? intFn(y) : booleanFn(y); // OK

        }
    }
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

function test4(A|B x, A|C y, B|C z) {
    _ = x is A ? aFn(x) : bFn(x); // error incompatible types: expected 'B', found '(A|B)'

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

function aFn(A x) {
}

function bFn(B x) {
}

function cFn(C x) {
}
