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

function testUnreachableCodeWithTypeNarrowing4() {
    int|string|boolean i = 1;

    if i is int|string {
        panic error("Error");
    }
    if i is boolean {
        return;
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
