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

function testTypeGuardTypeNarrow() returns boolean {
    test1({i: 3}, {j: "s"}, {i: "s"});
    test2(2);
    test3(());
    test4(4);
    test5(-4);
    test6(Z);
    test7();
    test8(1);
    test9(3.3);
    test10(2.3f);
    test11(3);
    test12(3.2);
    test13("s");
    test14(5);
    test15("s", true);
    test16(2);

    return true;
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

function test1(A|B x, A|C y, B|C z) {
    if x is A {
        A _ = x;
    } else {
        A|B _ = x; // No narrow
    }

    if y is A {
        A _ = y;
    } else {
        C _ = y; // OK
    }

    if z is B {
        B _ = z;
    } else {
        C _ = z; // OK
    }
}

function test2(int|string|boolean? b) {
    if b is string {
        string _ = b;
    } else if b is boolean {
        boolean _ = b;
    } else {
        int? _ = b;
    }
}

function test3(int|boolean|string|() x) {
    if x is int|boolean {
        if x is int {
            int _ = x;
        } else {
            boolean _ = x;
        }
    } else {
        if x is string {
            string _ = x;
        } else {
            () _ = x;
        }
    }
}

function test4(int|boolean|string? b) {
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
}

type Zero 0;

type PosInts 1|2|3|4;

type NegInts -1|-2|-3|-4;

type Ints PosInts|Zero|NegInts;

function test5(Ints x) {
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
        } else {
            -3|-4 _ = x;

            if x is (-3) {
                (-3) _ = x;
            } else {
                -4 _ = x;
            }
        }
    } else {
        Zero _ = x;
    }
}

enum E {
    X,
    Y,
    Z
}

function test6(E e) {
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

function test7() {
    int|string x = "x";

    do {
        if x is int {
            return;
        }

        string _ = x; // OK
    }

    int|string y = "y";

    {
        if y is int {
            return;
        }

        string _ = y; // OK
    }
}

function test8(1|"foo"|false? x) {
    if x == false {
        false _ = x;
    } else if "foo" == x {
        "foo" _ = x;
    } else {
        1? _ = x; // OK
    }
}

function test9(1|2.0|3.3? x) {
    if x == () {
        () _ = x;
    } else {
        1|2.0|3.3 _ = x; // OK

        if x is (2.0|3.3) {
            if (x == 2.0) {
                return;
            }

            3.3 _ = x; // OK
        } else {
            1|3.3 _ = x; // OK
        }
    }
}

function test10(int|string|float|boolean x) {
    if !(x is int) {
        float|string|boolean _ = x; // OK

        if !(x is string) {
            float|boolean _ = x; // OK

            if !(x is float) {
                boolean _ = x; // OK
                return;
            }
        }
    }
}

function test11(int|string|float x) {
    if !!!!(x !is int) {
        float|string _ = x; // OK
    } else {
        int _ = x; // OK
    }
}

function test12(int|string|float x) {
    if x is string|int && x is string|float {
        string _ = x;
        return;
    }

    int|float _ = x; // OK
}

function test13(int|string x) {
    if x is int && x == 2 {
        2 _ = x; // OK
    } else {
        string|int _ = x; // OK
    }

    if !(x !is int) {
        int _ = x; // OK
    } else {
        string _ = x; // OK
    }
}

function test14(int|string x) {
    if false || x is string {
        string|int _ = x;
    } else {
        int _ = x; // OK
    }

    if false || x is string {
        string|int _ = x;
        return;
    }

    int _ = x; // OK
}

function test15(int|string x, boolean|float y) {
    if x is int || y is float {
        int|string _ = x; // OK
        float|boolean _ = y; // OK
    } else {
        string _ = x; // OK
        boolean _ = y; // OK
    }
}

function test16(2|"foo"|"bar"? x) {
    if x == "foo" || x == "bar" {
        "foo"|"bar" _ = x; // OK
    } else if x == +2 {
        (+2) _ = x;
        2 _ = x;
    } else {
        () _ = x; // OK
    }

    ("bar"|2|"foo")? _ = x;
}
