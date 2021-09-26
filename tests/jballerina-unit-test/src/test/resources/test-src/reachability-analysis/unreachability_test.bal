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
        foo(); // unreachable code
        string b = "ABC";
        string c = b + "D";
    }
    int|string c = 25;
}

function testUnreachableCodeWithIf1() {
    if false {
        int a = 1; // unreachable code
        string b = "ABC";
    }
    int|string c = 25;
}

function testUnreachableCodeWithIf2() {
    if false {
        string b = "ABC"; // unreachable code
        foo();
        int a = 1;
    }
    int|string c = 25;
}

function testUnreachableCodeWithIf3() {
    if true {
        string b = "ABC";
        return;
    }
    int|string c = 25; // unreachable code
}

function testUnreachableCodeWithIfElse1() {
    if false {
        int a = 1; // unreachable code
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
    } else {
        string d = "XYZ"; // unreachable code
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
    } else if e is Z { // always true
        a = v * 30;
    } else {
        foo(); // unreachable code
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
    } else if e is Z { // always true
        a = v * 30;
    } else if e is Y {
        foo(); // unreachable code
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
    } else if e is Z { // always true
        a = v * 30;
        return ();
    } else {
        a = v * 40; // unreachable code
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
    // must return a result
}

function testUnreachableCodeWithIfElse7(E e) returns int {
    if e is X {
        return 1;
    } else if e is Y {
        return 2;
    }
    int a = 12;
    // must return a result
}

function testUnreachableCodeWithWhileHavingBreakAndContinue1() {
    int a;
    while false {
        if true { // unreachable code
            continue;
        } else {
            a = 1; // unreachable code
        }
        int i = a;
    }

    int b;
    while false {
        if true { // unreachable code
            break;
        } else {
            b = 1; // unreachable code
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
        if b is string { // always true
            value += 2;
            return value;
        }
        value += 3; // unreachable code
    }
    value += 4;
    // must return a result
}

function testUnreachableCodeWithWhile4() returns int {
    int value = 0;
    while value is int { // always true
        int|string b = 10;
        if b is int {
            value += 1;
            if false {
                break; // unreachable code
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
    value += 4;
    // must return a result
}

function testUnreachableCodeWithWhile5() returns int {
    int value = 0;
    while false {
        value += 1; // unreachable code
        foo();
        return value;
    }
    value += 2;
    // must return a result
}

function testUnreachableCodeWithFail() returns error? {
    boolean? v = false;
    int i = 0;
    string str = "";
    while i < 5 {
        i += 1;
        do {
            int|string a = "ABC";
            if a is string {
                if v is boolean {
                    break;
                } else if v is () { // always true
                    continue;
                }
                string x = "A"; // unreachable code
            }
            if a is int { // always true
                if true {
                    fail getError();
                }
            }
        }
    }
    string w = "A";
}

function testUnreachableCodeWithFail2() returns error {
    while true {
        int|string a = 12;
        if a is int {
            fail getError();
        }
        string b = a;
        if a is string { // always true
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
        if a is string { // always true
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

function testConstTrueConditionWihLogicalAndInIfElse() {
    if true && true {
        return;
    } else {
        int a = 10; // unreachable code
    }
}

function testConstTrueConditionWihLogicalAndInIf() {
    if true && true {
        return;
    }
    int a = 10; // unreachable code
}

function testConstTrueConditionWihLogicalAndInIfElse2() {
    if true && true {
        int a = 10;
    } else {
        int b = 10; // unreachable code
    }
    int c = 10;
}

function testConstTrueConditionWihLogicalAndInIfElse3() {
    if true {
        int a = 10;
    } else if true && true {
        int b = 10; // unreachable code
    }
    int c = 10;
}

function testConstFalseConditionWihLogicalAndInIfElse1() {
    if false && false {
        int a = 10; // unreachable code
    } else {
        int b = 10;
    }
    int c = 10;
}

function testConstTrueConditionWihLogicalAndInWhile1() {
    while true && true {
        if true && true {
            int a = 12;
        } else if true {
            int a = 10; // unreachable code
        }
    }
    int b = 10;
}

function testConstFalseConditionWihTypeTestInIfElse1() {
    int a = 10;
    if a !is int {
        int b = 10; // unreachable code
    } else {
        int c = 10;
    }
    int d = 10;
}

function testConstFalseConditionWihTypeTestInWhile1() {
    int a = 10;
    while a !is int {
        int b = 10; // unreachable code
    }
    int c = 10;
}

function impossible() returns never {
    panic error("Something impossible happened.");
}

function testConstFalseWithIf() {
    if false {
        int a = 10; // unreachable code
    } else {
        int a = 20;
    }
    int|string c = 25;
}

function testUnreachabilitywithIfElse(E e) returns int {
    if e is X {
        return 1;
    }
    if e is Y {
        return 2;
    }
    if e is Z { // always true
        return 3;
    }
    return 4; // unreachable code
}

function testConstFalseWithWhile() {
    while false {
        int a = 10; // unreachable code
    }
    int|string c = 25;
}

function testUnreachableCodeWithWhileConstTrue() {
    while true {
        string a = "A";
        return;
    }
    int b = 12; // unreachable code
}

function testUnreachableCodeWithWhileConstTrue2() returns int {
    while true {
        string? a = "A";
        if a is string {
            return 1;
        } else if a is () { // always true
            return 2;
        } else {
            return 3; // unreachable code
        }
    }
    return 4; // unreachable code
}

function testUnreachableCodeWithWhileConstTrue3() returns int {
    int|boolean v = true;
    int res = 0;
    while v is boolean {
        string? a = "A";
        if a is string {
            string|int b = "B";
            if b is string {
                if true {
                    return 1;
                }
                return 10; // unreachable code
            }
            if b is int { // always true
                break;
            }
            return 20; // unreachable code
        } else {
            return 2;
        }
        return 3; // unreachable code
    }
    return 4;
}

function testUnreachableCodeWithWhileConstTrue4(string p) returns string {
    string str = "";
    while true {
        int|string a = 12;
        if a is int {
            str += "a is int";
            break;
        }
        string b = a;
        if a is string { // always true
            str += "a is string";
            panic error("Error");
        }
        str += " -> final"; // unreachable code
    }
    string x = " -> end.";
    return str + x + " -> parameter:" + p;
}

const TRUE = true;
const FALSE = false;

function testUnreachableCodeWithConstRef1() {
    if TRUE {
        return;
    }
    int x = 1; // unreachable code
}

function testUnreachableCodeWithConstRef2() {
    if FALSE {
        return; // unreachable code
    }
    int x = 1;
}

function testUnreachableCodeWithConstRef3() {
    while TRUE {
        return;
    }
    int x = 1; // unreachable code
}

function testUnreachableCodeWithConstRef4() {
    while FALSE {
        return; // unreachable code
    }
    int x = 1;
}

type TRUE1 true;
type FALSE1 false;

function testUnreachableCodeWithConstRef5() {
    TRUE1 b = true;

    if b {
        return;
    }

    int x = 1; // unreachable code
}

function testUnreachableCodeWithConstRef6() {
    FALSE1 b = false;

    if b {
        return; // unreachable code
    }

    int x = 1;
}

function testUnreachableCodeWithConstRef7() {
    TRUE1 b = true;

    while b {
        return;
    }

    int x = 1; // unreachable code
}

function testUnreachableCodeWithConstRef8() {
    FALSE1 b = false;

    while b {
        return; // unreachable code
    }

    int x = 1;
}

function testUnreachableCodeWithUnaryCondition() {
    if !true {
        int a = 1; // unreachable code
    } else {
        int a = 2;
    }
}

function testUnreachableCodeWithUnaryCondition2() {
    while !true {
        int a = 1; // unreachable code
    }
    int b = 2;
}

function testUnreachableCodeWithBinaryCondition() {
    int x = 1;
    if true || x < 2 {
        int a = 1;
    } else {
        int a = 1; // unreachable code
    }
}

function testUnreachableCodeWithBinaryCondition2() {
    int x = 1;
    if x < 2 || true {
        int a = 1;
    } else {
        int a = 1; // unreachable code
    }
}

function testUnreachableCodeWithBinaryCondition3() {
    int x = 1;
    if true || true {
        int a = 1;
    } else {
        int a = 1; // unreachable code
    }
}

function testUnreachableCodeWithBinaryCondition4() {
    int x = 1;
    if false || false {
        int a = 1; // unreachable code
    } else {
        int a = 1;
    }
}
