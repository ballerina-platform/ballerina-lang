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

function test4(A|B x, A|C y, B|C z) {
    _ = x is A ? aFn(x) : bFn(x); // error incompatible types: expected 'B', found '(A|B)'

    _ = y is A ? aFn(y) : cFn(y); // OK

    _ = z is B ? bFn(z) : cFn(z); // OK
}

type D record {
    int i;
};

type E record {
    string i;
};

type F record {
    string j;
};

function dFn(D x) {
}

function eFn(E x) {
}

function fFn(F x) {
}

function test5(D|E x, D|F y, E|F z) {
    _ = x is D ? dFn(x) : eFn(x); // error incompatible types: expected 'E', found '(D|E)'

    _ = y is D ? dFn(y) : fFn(y); // error incompatible types: expected 'F', found '(D|F)'

    _ = z is E ? eFn(z) : fFn(z); // error incompatible types: expected 'F', found '(E|F)'
}

type G readonly & record {|
    string i;
|};

type H readonly & record {|
    int i;
|};


type I H|G;

function gFn(G x) {
}

function hFn(H x) {
}

function test6(I x) {
     _ = x is H ? hFn(x) : gFn(x); // OK
}

type S readonly & record {|
    int i;
|};

type J readonly & record {|
    string i;
|};

function sFn(S x) {
}

function jFn(J x) {
}

function test7(S|J x) {
    _ = x is S ? sFn(x) : jFn(x); // OK
}

type K record {|
    int i;
|};

type L record {|
    string i;
|};

type M readonly & (K|L);

function kFn(K x) {
}

function lFn(L x) {
}

function test8(M x) {
    _ = x is K ? kFn(x) : lFn(x); // OK
}

type N record {|
    int i;
|};

type P record {|
    string i;
|};

function nFn(N x) {
}

function pFn(P x) {
}

function test9(readonly & (N|P) x) {
    _ = x is N ? nFn(x) : pFn(x); // OK
}

type V record {|
    int i;
|};

type W record {|
    string s;
|};

type Y record {|
    int s;
|};

type Z record {|
    float f;
|};

function vwFn(V|W x) {
}

function yzFn(Y|Z x) {
}

function vwyFn(V|W|Y x) {
}

function zFn(Z x) {
}

function test10(V|W|Y|Z x) {
    _ = x is V|W ? vwFn(x) : yzFn(x); // error incompatible types: expected '(Y|Z)', found '(W|Y|Z)'
    _ = x is V|W|Y ? vwyFn(x) : zFn(x); // OK
}

type Q record {|
    string a;
|};

type R record {
    int b;
};

type T record {
    boolean a;
};

function qFn(Q x) {
}

function r2Fn(R x) {
}

function rtFn(R|T x) {
}

function test12(Q|R x) {
    _ = x is Q ? qFn(x) : r2Fn(x); // error incompatible types: expected 'R', found '(Q|R)'

    Q|R|T y = <Q>{a: ""};
    _ = y is Q ? qFn(y) : rtFn(y); // error incompatible types: expected '(R|T)', found '(Q|R|T)'
}
