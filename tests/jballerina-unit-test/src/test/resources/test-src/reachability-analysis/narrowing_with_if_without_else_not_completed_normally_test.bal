// Copyright (c) 2021 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testUnreachableCodeWithTypeNarrowing1() {
    int|string|boolean s = "foo";

    if s is int|string {
        if s is int {
            int x = s;
            return;
        }
        int y = s;
    }

    boolean z = s;
}

function testUnreachableCodeWithTypeNarrowing2() returns string {
    int|string|boolean i = 1;
    int jo = 0;
    int|string|boolean qo = 0;
    int|string|boolean ro = 0;

    if i is int|string {
        if true {
            if i is int {
                if true {
                    int j = i;
                    jo = j;
                    i = "hello";
                    return "int or string";
                }
            } else {
                string s = i;
                return "string";
            }
        }
    }
    int|string x = i;
}

function testUnreachableCodeWithTypeNarrowing3() {
    int|string|boolean i = 1;
    int|string|boolean qo = 0;

    if i is int|string {
        if qo is int|string {
            panic error("Error!");
        } else if qo is boolean {
            return;
        }
        boolean a = qo;
    }
    int x = i;
}

function testUnreachableCodeWithTypeNarrowing5() {
    int|string x = 1;

    if x is int {
        int i = 0;
        while i < 2 {
            int|string j = x;
            if j is int {
                x = "hello";
                return;
            }
            int m = j;
            if j is string {
                return;
            }
            i += 1;
            break;
        }
    }
    string y = x;
}

function testUnreachableCodeWithTypeNarrowing6() {
    int|string a = 1;
    int|string b = 1;

    if a is int {
        if a < 10 {
            while b is int {
                if b == 1 {
                    b += 1;
                    return;
                } else {
                    int c = a;
                    a = "hello";
                    panic error("Error");
                }
            }
        }
    }
    string y = a;
}

function testUnreachableCodeWithTypeNarrowing7() {
    int|string|boolean a = 1;
    int b = 1;

    if a is int|string {
        if a is int {
            while b is int {
                continue;
            }
        }
        int x = a;
    }
    string y = a;
}

function testUnreachableCodeWithTypeNarrowing8() {
    int|string x = 1;

    foreach int m in 1 ..< 2 {
        float|string a = "A";
        if a is string {
            x = "hello";
            return;
        }
        boolean j = a;
    }

    string y = x;
}

function testUnreachableCodeWithTypeNarrowing9() {
    int|string x = 1;

    if x is int {
        int i = 0;
        foreach int m in i ..< 2 {
            if i == 0 {
                x = "hello";
                return;
            }
        }
    }

    string y = x;
}

function testUnreachableCodeWithTypeNarrowing10() {
    int|string x = 1;

    if x is int {
        int? i = 0;
        if i is () {
            boolean? j = ();
            if j is () {
                panic error("error");
            }
            j = true;
            boolean k = j;
        }
        i = 1;
        int m = i;
    }

    string y = x;
}

function testTypeNarrowingWithIfWithoutElseNotCompletedNormally2() {
    int|string a = 1;
    int? b = 12;
    string? c = "A";
    if a is int {
        a = 2;
        return;
    }
    if b is () {
        return;
    }
    int y = a;
    if c is () {
        int x = 24;
        return;
    }
    a = "A";
    int x = a;

    string z = b;

    c = "B";
    string v = c;
}

function testTypeNarrowingWithIfWithoutElseNotCompletedNormally3() {
    10|20 b = 10;

    if b == 10 {
        return;
    }

    10 c = b;
}

function testTypeNarrowingWithIfWithoutElseNotCompletedNormally5() {
    10|20 b = 10;

    if b != 10 {
        return;
    }

    20 c = b;
}

type Type1 10|20;
type Type2 10;

function testTypeNarrowingWithIfWithoutElseNotCompletedNormally6() {
    Type1 b = 10;

    if b == 10 {
        return;
    }

    10 c = b;
}

function testTypeNarrowingWithIfWithoutElseNotCompletedNormally8() {
    Type1 b = 10;

    if b != 10 {
        return;
    }

    20 c = b;
}

function testTypeNarrowingWithIfWithoutElseNotCompletedNormally9() {
    int? a = 10;
    if !(a is int) {
        return;
    }
    () b = a;
    a = 1;
    int c = a;
}

function testTypeNarrowingWithIfWithoutElseNotCompletedNormally10() {
    int? a = 10;
    if a !is int {
        return;
    }
    () b = a;
    a = 1;
    int c = a;
}

function testTypeNarrowingWithIfWithoutElseNotCompletedNormally11() {
    Type1 b = 10;
    if !(b == 10) {
        return;
    }
    20 c = b;
    b = 10;
    10 d = b;
}

function testTypeNarrowingWithWhileNotCompletedNormally() {
    int? a = 10;
    if a is int {
        while true {
            if a == 10 {
                return;
            }
        }
    }
    int _ = a;
}

function testTypeNarrowingWithWhileNotCompletedNormally2() {
    int? a = 10;
    if a is int {
        while true {
            return;
        }
    }
    int _ = a;
}

function testTypeNarrowingWithWhileCompletedNormally() {
    int? a = 10;
    if a is int {
        while true {
            if a == 10 {
                break;
            }
        }
    }
    () b = a;
}

function testTypeNarrowingWithWhileCompletedNormally2() returns int? {
    int? a = 10;
    if a is int {
        int b = 1;
        while b < 5 {
            if a == 10 {
                return;
            }
            b += 1;
        }
    }
    () b = a;
}
