// Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function f1() {
    int|string x = 1;

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            x = "hello";
            i += 1;
            break;
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            if i < 0 {
                x = 1;
                break;
            }
            i += 1;
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            x = 1;
            i += 1;
            break;
        }
    }
}

function f2() {
    int|string x = 1;
    int|string y = 1;

    if x is int && y is int {
        int i = 0;
        while i < 2 {
            if i == 0 {
                x = "hello";
                y = 2;
                i += 1;
                break;
            }
            int j = <int> x + <int> y;
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            if i == 0 {
                x = "hello";
                i += 1;
                break;
            }
            int j = <int> x;
        }
    }
}

function f3() {
    int|string a = 1;
    int|string b = 1;

    while a is int {
        if a < 10 {
            while b is int {
                int c = a;
                a = "hello";
                break;
            }
        }
    }

    if a is int {
        if a < 10 {
            while b is int {
                int c = a;
                if b == 0 {
                    a = "hello";
                    break;
                }
            }
        }
    }
}

function f4() {
    int|string a = 1;
    int|string b = 1;

    while a is int {
        if a < 10 {
            while b is int {
                if b < 10 {
                    int c = a;
                    a = "hello";
                    break;
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            while b is int {
                if b == 1 {
                    b += 1;
                    break;
                } else {
                    int c = a;
                    a = "hello";
                    break;
                }
            }
        }
    }
}

function f5() {
    int|string x = 1;

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            x = "hello";
            break;
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            if m == 1 {
                x = 1;
                break;
            }
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            x = "hello";
            break;
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;

            if m > 0 {
                x = 1;
                break;
            }
        }
    }
}

function f6() {
    int|string x = 1;

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            if i == 0 {
                x = "hello";
                break;
            }
            int j = <int> x;
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            if i == 0 {
                x = "hello";
                break;
            }
            int j = <int> x;
        }
    }
}

function f7() {
    int|string a = 1;
    int|string b = 1;

    while a is int {
        if a < 10 {
            foreach int m in 0 ..< 2 {
                int c = a;
                a = "hello";
                break;
            }
        }
    }

    if a is int {
        if a < 10 {
            foreach int m in 0 ..< 2 {
                int c = a;
                if m == 0 {
                    a = "hello";
                    break;
                }
            }
        }
    }
}

function f8() {
    int|string a = 1;
    int|string b = 1;

    while a is int {
        if a < 10 {
            foreach int m in a ..< 2 {
                if m < 10 {
                    int c = a;
                    a = "hello";
                    break;
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            foreach int m in 1 ..< 2 {
                if b == 1 {
                    b += 1;
                    continue;
                } else {
                    int c = a;
                    a = "hello";
                    break;
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            foreach int m in 1 ..< 2 {
                if b == 1 {
                    int c = a;
                    a = "hello";
                    break;
                } else {
                    a = "hello";
                    break;
                }
            }
        }
    }
}

type Foo record {|
    int i;
    string s?;
|};

function f9() {
    Foo?[] arr = [{i: 1}, {i: 2, s: "b"}, (), {i: 3}];

    Foo? next = arr[0];
    int i = 1;

    while next is Foo {
        string s = next.toString();
        next = arr[i]; // OK, since the loop narrows it.
        i += 1;
    }
}

function f11() {
    int|string x = 1;

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            x = "hello";
            i += 1;
            panic error("error!");
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            if i < 0 {
                x = 1;
                return;
            }
            i += 1;
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            x = 1;
            i += 1;
            return;
        }
    }
}

function f12() {
    int|string x = 1;
    int|string y = 1;

    if x is int && y is int {
        int i = 0;
        while i < 2 {
            if i == 0 {
                x = "hello";
                y = 2;
                i += 1;
                panic error("error!");
            }
            int j = <int> x + <int> y;
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            if i == 0 {
                x = "hello";
                i += 1;
                return;
            }
            int j = <int> x;
        }
    }
}

function f13() {
    int|string a = 1;
    int|string b = 1;

    while a is int {
        if a < 10 {
            while b is int {
                int c = a;
                a = "hello";
                panic error("error!");
            }
        }
    }

    if a is int {
        if a < 10 {
            while b is int {
                int c = a;
                if b == 0 {
                    a = "hello";
                    return;
                }
            }
        }
    }
}

function f14() {
    int|string a = 1;
    int|string b = 1;

    while a is int {
        if a < 10 {
            while b is int {
                if b < 10 {
                    int c = a;
                    a = "hello";
                    panic error("error!");
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            while b is int {
                if b == 1 {
                    b += 1;
                    panic error("error!");
                } else {
                    int c = a;
                    a = "hello";
                    return;
                }
            }
        }
    }
}

function f15() {
    int|string x = 1;

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            x = "hello";
            return;
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            if m == 1 {
                x = 1;
                panic error("error!");
            }
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            x = "hello";
            panic error("error!");
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;

            if m > 0 {
                x = 1;
                panic error("error!");
            }
        }
    }
}

function f16() {
    int|string x = 1;

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            if i == 0 {
                x = "hello";
                return;
            }
            int j = <int> x;
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            if i == 0 {
                x = "hello";
                panic error("error!");
            }
            int j = <int> x;
        }
    }
}

function f17() {
    int|string a = 1;
    int|string b = 1;

    while a is int {
        if a < 10 {
            foreach int m in 0 ..< 2 {
                int c = a;
                a = "hello";
                panic error("error!");
            }
        }
    }

    if a is int {
        if a < 10 {
            foreach int m in 0 ..< 2 {
                int c = a;
                if m == 0 {
                    a = "hello";
                    return;
                }
            }
        }
    }
}

function f18() {
    int|string a = 1;
    int|string b = 1;

    while a is int {
        if a < 10 {
            foreach int m in a ..< 2 {
                if m < 10 {
                    int c = a;
                    a = "hello";
                    panic error("error!");
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            foreach int m in 1 ..< 2 {
                if b == 1 {
                    b += 1;
                    continue;
                } else {
                    int c = a;
                    a = "hello";
                    panic error("error!");
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            foreach int m in 1 ..< 2 {
                if b == 1 {
                    int c = a;
                    a = "hello";
                    panic error("error!");
                } else {
                    a = "hello";
                    return;
                }
            }
        }
    }
}

function f19() {
    int|string a = 1;
    int|string b = 1;

    if a is int {
        if a < 10 {
            foreach int m in 1 ..< 2 {
                match b {
                    1 => {
                        b = 2;
                        continue;
                    }

                    _ => {
                        int c = a;
                        a = "hello";
                        panic error("error!");
                    }
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            int m = 0;

            while m < 2 {
                match b {
                    1 => {
                        int c = a;
                        a = "hello";
                        panic error("error!");
                    }
                    _ => {
                        a = "hello";
                        return;
                    }
                }
            }
        }
    }
}

function f20() {
    int|string a = 1;
    int|string b = 1;

    if a is int && b is int {
        if a < 10 {
            foreach int m in 1 ..< 2 {
                int v = b;
                b = "str";
                match b {
                    1 => {
                        b = 2;
                        panic error("error!");
                    }

                    _ => {
                        int c = a;
                        a = "hello";
                        return;
                    }
                }
            }
        }
    }

    if a is int && b is int {
        if a < 10 {
            foreach int m in 1 ..< 2 {
                int v = b;
                b = "str";

                if b == 1 {
                    b = 2;
                    return;
                } else {
                    int c = a;
                    a = "hello";
                    panic error("error!");
                }
            }
        }
    }

    if a is int && b is int {
        if a < 10 {
            int m = 0;

            while m < 2 {
                int v = b;
                b = "str";
                match b {
                    1 => {
                        int c = a;
                        a = "hello";
                        panic error("error!");
                    }
                    _ => {
                        a = "world";
                        return;
                    }
                }
            }
        }
    }

    if a is int && b is int {
        if a < 10 {
            int m = 0;

            while m < 2 {
                int v = b;
                b = "str";

                if b == 1 {
                    int c = a;
                    a = "hello";
                    panic error("error!");
                } else {
                    a = "world";
                    return;
                }
            }
        }
    }
}

function f21() {
    int|string a = "str";
    int|string b = 1;
    int|string c = 1;
    int|string d = 1;
    any[] w = [];
    map<any> x = {};

    if a is string && b is int && c is int && d is int && w is int[] {
        while b < 5 {
            [string, int, int, int] [e, f, g, h] = [a, b, c, d];
            if b == 1 {
                [a, b, f, c, d, ...w] = [1, 2, 3, 4, 5];
                return;
            } else if b == 2 {
                [...w] = [];
                break;
            }
            w[1] = 1; // OK
        }
    }

    if a is string && b is int && c is int && d is int && x is map<int|string> {
        foreach int i in 0 ... 2 {
            [string, int, int, int] [e, f, g, h] = [a, b, c, d];
            record {
                string a;
                int b;
                string c;
                int d;
            } rec = {a: "", b: 0, c: "", d: 1};

            if f == 0 {
                {a: e, b: f, d} = rec;
                break;
            } else {
                {a, b, ...x} = rec;
                break;
            }
        }
    }

    if a is string && b is int && c is int && d is int && x is map<anydata> {
        foreach int i in 0 ... 2 {
            [string, int, int, int] [e, f, g, h] = [a, b, c, d];
            error<record { int b; string c; int d; }> err = error("error!", b = 1, c = "err!", d = 2);
            match f {
                12 => {
                    error(a, b = b, c = c, d = d) = err;
                    return;
                }
                _ => {
                    error(b = f, ...x) = err;
                    break;
                }
            }
        }
    }
}

function f22() {
    int|string a = 1;
    int|string b = "foo";

    if a is int && b is string {
        while a < 3 {
            function () fn = function () {
                int x = <int> a;
                a = "hello";
            };
            fn();
        }
    }
}
