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

function test1(int|string s) {
    if s is int {
        int _ = s;
    } else {
        string _ = s;
    }

    int _ = s; // error incompatible types: expected 'int', found '(int|string)'
}

function test2(int|string|boolean b) {
    if b is string {
        string _ = b;
    } else if b is boolean {
        boolean _ = b;
    } else {
        int _ = b;
    }

    int _ = b; // error incompatible types: expected 'int', found '(int|string|boolean)'
}

function test3(int|string|boolean? b) {
    if b is string {
        string _ = b;
    } else if b is boolean {
        boolean _ = b;
    } else {
        int? _ = b;
    }

    int _ = b; // error incompatible types: expected 'int', found '(int|string|boolean)?'
}

function test4(int|string s) {
    if s is int {
        int _ = s;
        return;
    }

    string _ = s;
}

function test5(int|string|boolean b) {
    if b is string {
        string _ = b;
        return;
    } else if b is boolean {
        boolean _ = b;
        return;
    }

    int _ = b; // type not narrowed. issue #34307
}

function test6(int|string|boolean? b) {
    if b is string? {
        string? _ = b;
        return;
    } else if b is boolean {
        boolean _ = b;
        return;
    }

    int _ = b; // type not narrowed. issue #34307
}

function test7(boolean? b) {
    if b is () {
        () _ = b;
        return;
    }

    boolean _ = b;
}

function test8(boolean? b) {
    if b is () {
        () _ = b;
        return;
    } else {
        boolean _ = b;
    }

    boolean _ = b; // type not narrowed. issue #34307
}

function test9(boolean? b) {
    if b is true {
        true _ = b;
        return;
    } else if b is false {
        false _ = b;
        return;
    } else {
        () _ = b; // type not narrowed. issue #30598, #33217
    }

    () _ = b; // type not narrowed. issue #34307
}

function test10(true|false? b) {
    if b is true {
        true _ = b;
        return;
    } else if b is false {
        false _ = b;
        return;
    } else {
        () _ = b;
    }

    () _ = b; // type not narrowed. issue #34307
}

function test11(int|boolean|string? b) {
    if b is string|boolean {
        string|boolean _ = b;
    } else if b is int|boolean {
        int _ = b;
    } else {
        () _ = b;
    }

    int _ = b; // error incompatible types: expected 'int', found '(int|boolean|string)?'
}

function test12(int|boolean|string|() x) {
    if x is int|boolean {
        if x is int {
            int _ = x;
        } else {
            boolean _ = x;
        }

        int _ = x; // error incompatible types: expected 'int', found '(int|boolean)'
    } else {
        if x is string {
            string _ = x;
        } else {
            () _ = x;
        }

        string _ = x; // error incompatible types: expected 'string', found 'string?'
    }

    int _ = x; // error incompatible types: expected 'int', found '(int|boolean|string)?'
}

function test13(int|boolean|string? b) {
    if b is () {
        () _ = b;
    } else {
        int|string|boolean _ = b;

        if b is string|boolean {
            string|boolean _ = b;

            if (b is boolean) {
                return;
            }

            string _ = b;
        } else {
            int _ = b;
        }
    }

    int|string? _ = b; // type not narrowed. issue #34307
}

type Zero 0;

type PosInts 1|2|3|4;

type NegInts -1|-2|-3|-4;

type Ints PosInts|Zero|NegInts;

function test14(PosInts|Zero|NegInts? n) {
    if n is PosInts {
        PosInts _ = n;
    } else if n is Zero {
        Zero _ = n;
    } else {
        NegInts? _ = n;
    }

    int _ = n; // error incompatible types: expected 'int', found '(PosInts|Zero|NegInts)?'
}

function test15(Ints? n) {
    if n is PosInts {
        PosInts _ = n;
    } else if n is Zero {
        Zero _ = n;
    } else {
        NegInts? _ = n;
    }

    int _ = n; // error incompatible types: expected 'int', found 'Ints?'
}

function test16(Ints x) {
    if x is 1|2|3|4 {
        PosInts _ = x;

        if x is 1|2 {
            1|2 _ = x;

            if x is 1 {
                1 _ = x;
            } else {
                2 _ = x;
            }
        } else {
            3|4 _ = x;

            if x is 3 {
                3 _ = x;
            } else {
                4 _ = x;
            }
        }
    } else if x is -1|(-2)|(-4)|-3 {
        NegInts _ = x;

        if x is (-1|-2) {
            -1|-2 _ = x;

            if x is -1 {
                -1 _ = x;
            } else {
                -2 _ = x;
                return;
            }

            -1 _ = x; // type not narrowed. issue #34307
        } else {
            -3|-4 _ = x;

            if x is (-3) {
                (-3) _ = x;
            } else {
                -4 _ = x;
            }

            -3 _ = x; // error incompatible types: expected '-3', found '(-4|-3)'
        }
    } else {
        Zero _ = x;
    }

    0 _ = x; // error incompatible types: expected '0', found 'Ints'
}

function test17(int|string|() x) {
    if x is int|string {
        int|string _ = x;
        if x is int {
            return;
        }

        string _ = x;
    }

    string? _ = x; // type not narrowed. issue #34307
}

function test18(int|string|boolean|() x) {
    if x is int|string {
        int|string _ = x;
        if x is int {
            return;
        }

        string _ = x;
    } else if x is () {
        () _ = x;
        return;
    } else {
        boolean _ = x;
        return;
    }

    string _ = x; // type not narrowed. issue #34307
}

function test19(int|boolean|string? x) {
    if x is int {
        int _ = x;
        return;
    }

    if x is string {
        string _ = x;
        return;
    }

    if x is boolean {
        boolean _ = x;
        return;
    }

    () _ = x;
}

function test20(int|boolean|string? x) {
    if x is int {
        int _ = x;
        return;
    } else if x is string {
        string _ = x;
        return;
    } else if x is boolean {
        boolean _ = x;
        return;
    }

    () _ = x; // type not narrowed. issue #34307
}

function test21(readonly & int[]|string[]? a) {
    if a is int[] {
        int[] _ = a;
    } else {
        string[]? _ = a;
    }

    int _ = a; // error incompatible types: expected 'int', found '((int[] & readonly)|string[])?'
}

function test22(int[]|string[]? a) {
    if a is int[] {
        int[] _ = a;
    } else {
        string[]? _ = a; // error incompatible types: expected 'string[]?', found '(int[]|string[])?'
    }

    int _ = a; // error incompatible types: expected 'int', found '(int[]|string[])?'
}

type T readonly & record {|int i;|}?;

function test23(T t) {
    if t is () {
        () _ = t;
    } else {
        int _ = t.i;
    }
}

type R record {|
    int i;
|};

readonly class Foo {
    R? r;

    function init(int? n) {
        if n is int {
            int _ = n;
            self.r = {i: n};
        } else {
            () _ = n;
            self.r = ();
        }
    }
}

function test24() {
    Foo foo = new (3);

    var result = foo.r;
    if result !is () {
        R _ = result;
        return;
    }

    () _ = result;
}

class Bar {
    R? r;

    function init(int? n) {
        if n is int {
            int _ = n;
            self.r = {i: n};
        } else {
            () _ = n;
            self.r = ();
        }
    }
}

function test25() {
    Bar bar = new (3);

    var result = bar.r;
    if result !is () {
        R _ = result; // should not narrow. issue #33722
        return;
    }

    () _ = result; // should not narrow. issue #33722
}

type L record {|
    string a;
|};

type M record {|
    int b;
|};

type N record {|
    int a;
|};

function test26(L|M r) {
    if r is L {
        L _ = r;
    } else {
        M _ = r; // should not narrow. issue #33722
    }

    M _ = r; // error incompatible types: expected 'M', found '(L|M)'
}

function test27(L|M r) {
    if r is L {
        return;
    }

    M _ = r; // should not narrow. issue #33722
}

function test28(L|N r) {
    if r is L {
        L _ = r;
    } else {
        M _ = r; // OK
    }

    L _ = r; // error incompatible types: expected 'L', found '(L|N)'
}

function test29(L|N r) {
    if r is L {
        return;
    }

    N _ = r; // OK
}

function test30(M|N r) {
    if r is N {
        N _ = r;
    } else {
        M _ = r; // OK
    }

    N _ = r; // error incompatible types: expected 'N', found '(M|N)'
}

function test31(N|M r) {
    if r is N {
        return;
    }

    M _ = r; // should not narrow. issue #33722
}

enum E {
    X,
    Y,
    Z
}

function test32(E e) {
    if e is X {
        X _ = e;
    } else if e is Y {
        Y _ = e;
    } else {
        Z _ = e;
    }

    if e is "X" {
        X _ = e;
    } else if e is "Y" {
        Y _ = e;
    } else {
        "Z" _ = e;
    }

    if e is "X" {
        return;
    }

    "Y"|Z _ = e;
}

function test33() {
    string? x = "hello";

    if x is string {
        var f = function() {
            x = ();
        };
        f();
        string _ = x; // No error. issue #30567
    }
}

function test34() {
    int|string x = "x";

    do {
        if x is int {
            return;
        }

        string _ = x; // OK
    }

    string _ = x; // Type not narrowed. issue #34307

    int|string y = "y";

    {
        if y is int {
            return;
        }

        string _ = y; // OK
    }

    string _ = y; // Type not narrowed. issue #34307
}
