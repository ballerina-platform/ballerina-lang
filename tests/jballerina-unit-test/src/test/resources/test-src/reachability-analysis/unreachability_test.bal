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

function testUnreachableCodeWithWhile1() {
    while false {
        int a = 1; // unreachable code
        string b = "ABC";
    }
    int|string c = 25;
}

function testUnreachableCodeWithWhile2() {
    while false {
        error:unreachable();
        foo();
        string b = "ABC";
        string c = b + "D";
    }
    int|string c = 25;
}

function testUnreachableCodeWithIf1() {
    if false { // unreachable code
        int a = 1;
        string b = "ABC";
    }
    int|string c = 25;
}

function testUnreachableCodeWithIf2() {
    if false { // unreachable code
        error:unreachable();
        string b = "ABC";
        foo();
        int a = 1;
    }
    int|string c = 25;
}

function testUnreachableCodeWithIf3() {
    if true {
        error:unreachable(); // not unreachable
        string b = "ABC";
        error:unreachable(); // not unreachable
        int a = 1;
    }
    int|string c = 25;
}

function testUnreachableCodeWithIfElse1() {
    if false { // unreachable code
        int a = 1;
        string b = "ABC";
    } else {
        string d = "XYZ";
        int e = 26;
    }
    int|string c = 25;
}

function testUnreachableCodeWithIfElse2() {
    if true {
        int a = 1;
        string b = "ABC";
        error:unreachable(); // not unreachable
    } else {
        string d = "XYZ";
        int e = 26;
    }
    int|string c = 25;
}

function testUnreachableCodeWithIf4() {
    if true {
        int a = 1;
        string b = "ABC";
        return;
    }
    int|string c = 25; // unreachable code
    int d = 10;
}

function testUnreachableCodeWithIf5() {
    if true {
        int a = 1;
        string b = "ABC";
        return;
        foo(); // unreachable code
    }
    int|string c = 25;
    int d = 10;
}

function foo() {

}

enum E { X, Y, Z }

function testUnreachableCodeWithIfElse3() {
    E e = "Z";
    int v = 10;
    int a;

    if e is X {
        a = v * 10;
    } else if e is Y {
        a = v * 20;
    } else if e is Z {
        a = v * 30;
    } else { // unreachable code
        foo();
    }
}

function testUnreachableCodeWithIfElse4() {
    E e = "Z";
    int v = 10;
    int a;

    if e is X {
        a = v * 10;
    } else if e is Y {
        a = v * 20;
    } else if e is Z {
        a = v * 30;
    } else if e is Y { // unreachable code
        foo();
    }
}

function testUnreachableCodeWithIfElse5() {
    E e = "Z";
    int v = 10;
    int a;

    if e is X {
        a = v * 10;
    } else if e is Y {
        a = v * 20;
    } else {
        error:unreachable(); // not unreachable
    }
}

function testUnreachableCodeWithIfElse6(E e) returns int {
    if e is X {
        return 1;
    }
    if e is Y {
        return 2;
    }
    int a = 12;
    error:unreachable(); // not unreachable
    // must return a result
}

function testUnreachableCodeWithIfElse7(E e) returns int {
    if e is X {
        return 1;
    } else if e is Y{
        return 2;
    }
    int a = 12;
    error:unreachable(); // not unreachable
    // must return a result
}

function testUnreachableCodeWithWhileHavingBreakAndContinue1() {
    int a;
    while false { // unreachable code
        if true {
            continue;
        } else { // unreachable code
            a = 1;
        }
        int i = a;
    }

    int b;
    while false { // unreachable code
        if true {
            break;
        } else { // unreachable code
            b = 1;
        }
        int i = b;
    }
}

function testUnreachableCodeWithWhile3() returns int {
    int value = 0;
    while true {
        int|string b = 10;
        if b is int {
            value += 1;
            break;
        }
        if b is string {
            value += 2;
            return value;
        }
        value += 3; // unreachable code
    }
    error:unreachable(); // not unreachable
    value += 4;
    // must return a result
}

function testUnreachableCodeWithWhile4() returns int {
    int value = 0;
    while value is int {
        int|string b = 10;
        if b is int {
            value += 1;
            if false { // unreachable code
                break;
            }
            value += 2;
        }
        if b is string {
            if true {
                continue;
            }
            value += 3; // unreachable code
        }
        value += 4;
    }
    error:unreachable(); // not unreachable
    value += 4;
    // must return a result
}

function testUnreachableCodeWithWhile5() returns int {
    int value = 0;
    while false { // unreachable code
        value += 1;
        foo();
        return value;
    }
    error:unreachable(); // not unreachable
    value += 2;
    // must return a result
}

function testUnreachableCodeWithFail() returns error? {
    boolean? v = false;
    int i = 0;
    string str = "";
    while true {
        do {
            int|string a = "ABC";
            if a is string {
                if v is boolean {
                    break;
                } else if v is () {
                    continue;
                }
                string x = "A"; // unreachable code
            }
            if a is int {
                if true {
                    fail getError();
                }
            }
        }
    }
    string w = "A"; // unreachable code
}

function testUnreachableCodeWithFail2() returns error {
    while true {
        int|string a = 12;
        if a is int {
            fail getError();
        }
        string b = a;
        if a is string {
            panic error("Error");
        }
    }
    string x = "ABC"; // unreachable code
}

function testUnreachableCodeWithCallStmtFuncReturningNever() {
    impossible();
    string x = "ABC"; // unreachable code
}

function testUnreachableCodeWithWhile6() {
    string str = "";
    int i = 1;
    while i < 5 {
        int|string a = 12;
        if a is int {
            str += "a is int -> ";
            panic getError();
        }
        string b = a;
        if a is string {
            str += "a is string";
            impossible();
        }
        string x = "ABC"; // unreachable code
        i += 1;
    }
    string y = "ABC";
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}

function testUnreachableCodeWithForeach() returns string {
    string str = "";
    int i = 1;
    foreach int idx in 1...5 {
        int|string a = 12;
        while true {
            if a is int {
                str += "a is int -> ";
                break;
            }
        }
        error:unreachable(); // not unreachable
    }
    str += "end.";
    return str;
}

function impossible() returns never {
    panic error("Something impossible happened.");
}