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

type A record {|
    string a;
|};

type B record {|
    int a;
|};

function f1(A|B x) {
    if x is A {
        return;
    } else {
        B _ = x; // error
    }
}

function f2(A|B x) {
    if x is A {
        return;
    }
    B _ = x; // error
}

type C record {|
    int i;
|};

type D record {|
    string i;
|};

type E C|D;

function f3(E v) {
    if v is C {

    } else {
        D _ = v; // error
    }
}

type F readonly & record {|
    int i;
|};

type G readonly & record {|
    string i;
|};

type H F|G;

function f4(H v) {
    if v is F {

    } else {
        G _ = v; // OK
    }
}

type I readonly & record {|
    int i;
|};

type J readonly & record {|
    string i;
|};

function f5(I|J v) {
    if v is I {

    } else {
        J _ = v; // OK
    }
}

type K record {|
    int i;
|};

type L record {|
    string i;
|};

type M readonly & (K|L);

function f6(M v) {
    if v is K {

    } else {
        L _ = v; // OK
    }
}

type N record {|
    int i;
|};

type O record {|
    string i;
|};

function f7(readonly & (N|O) v) {
    if v is N {

    } else {
        O _ = v; // OK, but not supported yet (snapshot 1)
    }
}

type P record {|
    int i;
|};

type Q record {|
    string i;
|};

type R record {|
    anydata i;
|};

function f8(P|Q v) {
    if v is R {

    } else {
        _ = v; // unreachable
    }
}

type S record {|
    int i;
|};

type T record {|
    int j;
|};

type U S|T;

function f9(U v) {
    if v is S {

    } else {
        T _ = v; // OK
    }
}

function f10() {
    int[]|error res = [];

    if res is int[] {
        int[] _ = res;
    } else {
        error x = res; // OK
    }
}

function f11() {
    int[] & readonly|string[] x = [1];

    if x is int[] {

    } else {
        string[] _ = x; // OK
    }
}

type V record {|
    int i;
|};

type W record {|
    string s;
|};

type X record {|
    int s;
|};

type Y record {|
    float f;
|};

function f12(V|W|X|Y v) {
    if v is V|W {

    } else {
        X|Y _ = v; // error
    }

    if v is V|W|X {

    } else {
        Y _ = v; // OK
    }
}
