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

function test1(int|string|true? x) {
    while x is int {
        int _ = x;
        break;
    }

    while x == true {
        true _ = x;
        break;
    }

    while x != true {
        int|string? _ = x; // OK
        break;
    }

    while !(x == true) {
        int|string? _ = x; // OK
        break;
    }

    while !(x == ()) {
        int|string|boolean _ = x; // OK
        break;
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|string|true)?'
}

function test2(int|string x, boolean|float y) {
    while x is int && y is float {
        int _ = x; // OK
        float _ = y; // OK
    }

    while x is int || y is float {
        int _ = x; // incompatible types: expected 'int', found '(int|string)'
        int|string _ = x; // OK

        float _ = y; // incompatible types: expected 'float', found '(float|boolean)'
        float|boolean _ = y; // OK
    }

    while true && x is int {
        int _ = x; // OK
    }

    while !(true && x is int) {
        int _ = x; // error incompatible types: expected 'int', found '(int|string)'
    }

    while true || x is int {
        int _ = x; // error incompatible types: expected 'int', found '(int|string)'
    }

    while !(false || x is int) {
        string _ = x; // OK
    }
}

function test3(int|string x) {
    while x is int {
        int _ = x;
        return;
    }

    string _ = x; // error incompatible types: expected 'int', found '(int|string)'
}

function test4(int|string? x) {
    while x != () {
        if x is int {
            int _ = x;
        } else {
            string _ = x; // OK
        }
        break;
    }

    while x != () {
        if x is int {
            int _ = x;
            return;
        }

        string _ = x; // OK
    }

    if x is int? {
        while x != () {
            int _ = x; // OK
        }
    }

    if x !is int? {
        string _ = x;

    } else {
        int? _ = x;

        while x != () {
            int _ = x; // OK
        }
    }
}

function test5(int|string|boolean? x) {
    while x !is () {
        while !(x is string) {
            while !(x is boolean) {
                int _ = x; // OK
            }
        }
    }
}

function test6(string|2|true? x) {
    while x !is () {
        while x != true {
            while !(x == 2) {
                string _ = x; // OK
            }
        }
    }
}

type A record {|
    int i;
|};

type B record {|
    string i;
|};

function test7(A|B x) {
    while x !is A {
        A _ = x; // error incompatible types: expected 'A', found 'B'
    }
}

type C A|B;

function test8(C v) {
    while v !is B {
        B _ = v; // error incompatible types: expected 'B', found 'A'
    }
}

type D readonly & record {|
    int i;
|};

type E readonly & record {|
    string i;
|};

function test9(D|E v) {
    while v !is D {
        E _ = v; // OK
    }
}

type F D|E;

function test10(F v) {
    while v !is D {
        E _ = v; // OK
    }
}

type G record {|
    int i;
|};

type H record {|
    string i;
|};

type J readonly & (G|H);

function test11(J v) {
    while v !is G {
        H _ = v; // OK
    }
}

function test12(readonly & (G|H) v) {
    while v !is G {
        H _ = v; // OK
    }
}

type N record {|
    int i;
|};

type P record {|
    int j;
|};

type Q N|P;

function test13(Q v) {
    if v is N {

    } else {
        P _ = v; // OK
    }
}

function test14() {
    int[]|error res = [];

    while res !is int[] {
        error x = res; // OK
    }
}

function test15() {
    int[] & readonly|string[] x = [1];

    while x !is int[] {
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

function test16(V|W|X|Y v) {
    while !(v is V|W) {
        X|V _ = v; // error incompatible types: expected '(X|V)', found '(X|Y)'
    }

    while v !is V|W|X {
        Y _ = v; // OK
    }
}

function test17([int]|[string] x) {
    while x !is [int] {
        [int] _ = x; // error incompatible types: expected '[int]', found '[string]'
        [string] _ = x; // OK
        [int|string] _ = x; // OK
        [int]|[string] _ = x; // OK
    }
}

function test18(([int]|[string]) & readonly x) {
    while x !is [int] {
        [string] _ = x; // OK
        [string] & readonly _ = x; // OK
        [int] _ = x; // error incompatible types: expected '[int]', found '([string] & readonly)'
    }
}

function test19(int[]|string[] x) {
    while x !is int[] {
        int[] _ = x; // error incompatible types: expected 'int[]', found 'string[]'
        string[] _ = x; // OK
        (int|string)[] _ = x; // OK
        int[]|string[] _ = x; // OK
    }
}

function test20(int[] & readonly|string[] & readonly x) {
    while x !is int[] {
        string[] _ = x; // OK
        int[] _ = x; // error incompatible types: expected 'int[]', found '(string[] & readonly)'
        string[] & readonly _ = x; // OK
    }
}

function test21() {
    int|boolean|string x = "x";
    while x !is int {
        do {
            if x is boolean {
                return;
            }

            string _ = x;
        }

        string _ = x; // Type not narrowed. issue #34307
    }

    int|boolean|string y = "y";
    while y !is int {
        {
            if y is boolean {
                return;
            }

            string _ = y;
        }

        string _ = y; // Type not narrowed. issue #34307
    }
}
