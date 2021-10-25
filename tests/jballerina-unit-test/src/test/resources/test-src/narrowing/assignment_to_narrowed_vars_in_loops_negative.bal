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
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            x = 1;
            i += 1;
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            x = "hello";
            i += 1;
            continue;
        }
    }

    if x is int {
        int i = 0;
        while i < 2 {
            int j = x;
            x = 1;
            i += 1;
            continue;
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
                continue;
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
                continue;
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
            }
        }
    }

    if a is int {
        if a < 10 {
            while b is int {
                int c = a;
                a = "hello";
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
                    continue;
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            while b is int {
                if b == 1 {
                    b += 1;
                    continue;
                } else {
                    int c = a;
                    a = "hello";
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
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            x = 1;
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            x = "hello";
            continue;
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            int j = x;
            x = 1;
            continue;
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
                continue;
            }
            int j = <int> x;
        }
    }

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            if i == 0 {
                x = "hello";
                continue;
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
            }
        }
    }

    if a is int {
        if a < 10 {
            foreach int m in 0 ..< 2 {
                int c = a;
                a = "hello";
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
                    continue;
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
                }
            }
        }
    }
}

function f9() {
    int|string a = 1;
    int|string b = 1;

    if a is int {
        if a < 10 {
            foreach int m in 1 ..< 2 {
                if b == 1 {
                    int c = a;
                    a = "hello";
                } else {
                    a = "hello";
                    return;
                }
            }
        }
    }

    if a is int {
        if a < 10 {
            int m = 0;
            while m < 2 {
                if b == 1 {
                    int c = a;
                    a = "hello";
                    panic error("error!");
                } else {
                    a = "hello";
                }
                m += 1;
            }
        }
    }
}

function f10() {
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
                    }
                    _ => {
                        a = "world";
                        return;
                    }
                }
            }
        }
    }
}

function f11() {
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
                        continue;
                    }

                    _ => {
                        int c = a;
                        a = "hello";
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
                    continue;
                } else {
                    int c = a;
                    a = "hello";
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
                } else {
                    a = "world";
                    return;
                }
            }
        }
    }
}

function f12() {
    int|string a = "str";
    int|string b = 1;
    int|string c = 1;
    int|string d = 1;
    any[] w = [];
    map<any> x = {};

    if a is string && b is int && c is int && d is int && w is int[] {
        while b < 5 {
            [string, int, int, int] [e, f, g, h] = [a, b, c, d];
            [a, b, f, c, d, ...w] = [1, 2, 3, 4, 5];
            [...w] = [];
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
            {a: e, b: f, d} = rec;
            {a, b, ...x} = rec;
        }
    }

    if a is string && b is int && c is int && d is int && x is map<anydata> {
        foreach int i in 0 ... 2 {
            [string, int, int, int] [e, f, g, h] = [a, b, c, d];
            error<record { int b; string c; int d; }> err = error("error!", b = 1, c = "err!", d = 2);
            error(a, b = b, c = c, d = d) = err;
            error(b = f, ...x) = err;
        }
    }
}

function f13() {
    record {} logRecord = {
        "module": 1234
    };

    foreach [string, anydata] [k, v] in logRecord.entries() {
        string|int value;
        match k {
            "module" => {
                value = v.toString();
                if value is string {
                    while k == "module" {
                        string str = value;
                        value = 4;
                    }
                }
            }
        }
    }
}
