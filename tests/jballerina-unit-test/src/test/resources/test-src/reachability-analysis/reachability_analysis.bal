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

function testReachableCodeWithIf2() {
    string a = "";
    if true {
        a = "ABC";
        string b = a;
        a += b;
    }
    int|string c = a + "DEF";
    assertEqual(c, "ABCABCDEF");
}

function testReachableCodeWithIf3() {
    assertEqual(getStringValue(), "ABCABC");
}

function getStringValue() returns string {
    string a = "";
    if true {
        a = "ABC";
        string b = a;
        a += b;
        return a;
    }
}

function testReachableCodeWithIfElse1() {
    string a = "";
    if true {
        a = "ABC";
        string b = a;
        a += bam(b);
    }
    int|string c = a + "GHI";
    assertEqual(c, "ABCABCDEFGHI");
}

function bam(string v) returns string {
    return v + "DEF";
}

enum E { X, Y, Z }

string msg = "";

function testReachableCodeWithIfElse2() {
    E e = "Z";
    doSomething(e);
    assertEqual(msg, "This is Z.End.");

    doSomething2(e);
    assertEqual(msg, "This is Z.End.");

    doSomething3(e);
    assertEqual(msg, "This is Z.End.");
}

function doSomething(E e) {
    if e is X {
        doX();
    } else if e is Y {
        doY();
    } else if e is Z {
        doZ();
    }
    msg += "End.";
}

function doSomething2(E e) {
    msg = "";
    if e is X {
        doX();
    } else if e is Y {
        doY();
    } else if e is Z {
        doZ();
    }
    msg += "End.";
}

function doSomething3(E e) {
    msg = "";
    if e is X {
        doX();
    } else if e is Y {
        doY();
    } else {
        doZ();
    }
    msg += "End.";
}


function doX() {
    msg = "This is X.";
}

function doY() {
    msg = "This is Y.";
}

function doZ() {
    msg = "This is Z.";
}

function testReachableCodeWithIfElse3() {
    E e = "Z";
    int res = getIntValue(e);
    assertEqual(res, 3);
}

function getIntValue(E e) returns int {
    if e is X {
        return 1;
    }
    if e is Y {
        return 2;
    }
    if e is Z {
        return 3;
    }
}

function testReachableCodeWithIfElse4() {
    int|string res = getValue();
    assertEqual(res, 12);
}

function getValue() returns int|string {
    string? e = "ABC";
    if e is string {
        int a = 12;
        if a is int {
            return a;
        }
    } else {
        string a = "DEF";
        if true {
            return a;
        }
    }
}

function testReachableCodeWithNestedIfElse1() {
    string res = getTypeAsString();
    assertEqual(res, "int or string");
}

function getTypeAsString() returns string {
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
    if i is boolean {
        return "boolean";
    }
}

function testReachableCodeWithIfElseAndConditionalExpr() {
    string res = getTypeAsString2();
    assertEqual(res, "xml");
}

function getTypeAsString2() returns string {
    int|string|float|boolean|xml x = xml `ABC`;
    if (x is boolean) {
        return "boolean";
    }
    if (x is int|string|float) {
        return x is int ? "int" : (x is float ? "float" : x);
    } else {
        xml y = x;
        return "xml";
    }
}

function testReachableCodeWithIfElseAndConditionalExpr2() {
    string res = findTypes(true);
    assertEqual(res, "boolean -> after if block1 -> boolean in else -> after all checks -> after true block2");

    res = findTypes(10.5);
    assertEqual(res, "float in if -> after true block1 -> after if block1 -> float in else -> " +
                "after all checks -> after true block2");

    res = findTypes(12);
    assertEqual(res, " -> after if block1 -> int -> after all checks -> after true block2");

    res = findTypes("A");
    assertEqual(res, "string in if -> after true block1 -> after if block1 -> string in else -> " +
                "after all checks -> after true block2");

    res = findTypes(xml `ABC`);
    assertEqual(res, "xml");
}

function findTypes(int|string|float|boolean|xml x) returns string {
    string str = "";
    if(x is boolean) {
        str += "boolean";
    }
    if (x is int|string|float|boolean) {
        if true {
            if x is string|float {
                if true {
                    str += x is float ? "float in if" : "string in if";
                }
                str += " -> after true block1";
            }
            str += " -> after if block1";
            if x is int {
                str += " -> int";
            } else {
                str += x is float ? " -> float in else" : x is string ? " -> string in else" : " -> boolean in else";
            }
            str += " -> after all checks";
        }
        string s = " -> after true block2";
        str += s;
    } else {
        xml y = x;
        str += "xml";
    }
    return str;
}

function testReachableCodeWithWhile2() {
    while true {
        string a = "A";
        return;
    }
}

function testReachableCodeWithWhile3() {
    int value = 0;
    while true {
        string a = "A";
        int|string b = 10;
        if b is int {
            value += 1;
            break;
        }
    }
    value += 2;
    assertEqual(value, 3);
}

function testReachableCodeWithWhile4() {
    assertEqual(foo1(), 1);
    assertEqual(foo2(), 1);
}

function foo1() returns int {
    while true {
        string? a = "A";
        if a is string {
            return 1;
        } else {
            return 2;
        }
    }
}

function foo2() returns int {
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
            }
            if b is int {
                break;
            }
        } else {
            return 2;
        }
        return 3;
    }
    return 4;
}

function testReachableCodeWithFail() returns error? {
    boolean? v = false;
    int i = 0;
    string str = "";
    while true {
        do {
            int|string a = "ABC";
            if a is string {
                if v is boolean {
                    str += a;
                    break;
                } else if v is () {
                    i += 1;
                    panic getError();
                }
            }
            if a is int {
                if true {
                    str += "DEF";
                    fail getError();
                }
            }
        }
    }
}

function testWhileCompletingNormally() {
    assertEqual(foo3("ABC"), "a is int -> end. -> parameter:ABC");
    string|error res = foo4();
    assertEqual(res is string, true);
    if (res is string) {
        assertEqual(res, "a is int -> Loop continued with digit: 1 -> Loop continued with digit: 2 -> " +
                         "Loop continued with digit: 3 -> Loop broke with digit: 4");
    }
    assertEqual(foo5(), "a is string -> ABCDEF -> a is string -> ABCDEF -> end.");
    assertEqual(foo6(), "a is int -> end.");
}

function foo3(string p) returns string {
    string str = "";
    while true {
        int|string a = 12;
        if a is int {
            str += "a is int";
            break;
        }
        string b = a;
        if a is string {
            str += "a is string";
            panic error("Error");
        }
    }
    string x = " -> end.";
    return str + x + " -> parameter:" + p;
}

function foo4() returns error|string {
    string str = "";
    while true {
        int|string a = 12;
        if a is int {
            str += "a is int -> ";
            fail error("Fail");
        }
        string b = a;
        if a is string {
            str += "a is string";
            panic error("Panic");
        }
    } on fail error e1 {
        foreach int digit in 1 ... 5 {
            do {
                fail getError();
            } on fail error e2 {
                if (digit < 4) {
                    str += "Loop continued with digit: " + digit.toString() + " -> ";
                    continue;
                } else {
                    str += "Loop broke with digit: " + digit.toString();
                    break;
                }
            }
        }
        return str;
    }
}

function foo5() returns string {
    string str = "";
    int i = 1;
    while i < 3 {
        int|string a = "DEF";
        if a is int {
            str += "a is int -> ";
            panic getError();
        }
        string b = a;
        if a is string {
            str += "a is string";
        }
        str += " -> ABC" + b + " -> ";
        i += 1;
    }
    str += "end.";
    return str;
}

function foo6() returns string {
    string str = "";
    int i = 1;
    while i < 5 {
        int|string a = 12;
        if a is int {
            str += "a is int -> ";
            break;
        }
        string b = a;
        if a is string {
            str += "a is string -> ";
            continue;
        }
    }
    str += "end.";
    return str;
}

function testCallStmtFuncReturningNever() {
    error? e = trap foo7();
    assertEqual(e is error, true);
    if (e is error) {
        assertEqual(e.message(), "Something impossible happened.");
    }
}

function foo7() {
    impossible();
}

function testForeachCompletingNormally() {
    assertEqual(foo8(), "a is int -> end.");
    assertEqual(foo9(), "a is int -> outside while -> a is int -> outside while -> a is int -> outside while -> " +
                        "a is int -> outside while -> a is int -> outside while ->  -> end.");
}

function foo8() returns string {
    string str = "";
    int i = 1;
    foreach int idx in 1...5 {
        int|string a = 12;
        if a is int {
            str += "a is int -> ";
            break;
        }
        string b = a;
        if a is string {
            str += "a is string -> ";
            continue;
        }
    }
    str += "end.";
    return str;
}

function foo9() returns string {
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
        str += "outside while -> ";
    }
    str += " -> end.";
    return str;
}

function testReachableCodeWithForeach() returns string {
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
    }
    str += "end.";
    return str;
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
}

function impossible() returns never {
    panic error("Something impossible happened.");
}

function testReachableCodeWithBinaryCondition() {
    int x = 1;
    if false || x < 2 {
        int a = 1;
    } else {
        int a = 1;
    }

    int b = 2;
}

function testReachableCodeWithBinaryCondition2() {
    int x = 1;
    if x < 2 {
        int a = 1;
    } else if x < 4 || false {
        int a = 1;
    } else {
        int a = 2;
    }

    int b = 2;
}

type BoolValue true|false;

function testReachableCodeWithBinaryCondition3() {
    int x = 1;
    int y = 1;
    BoolValue z = true;
    boolean v = true;
    if x < 1 && y < 1 {
        int a = 1;
    } else if x != 2 {
        int a = 3;
    } else if z {
        int a = 2;
    } else if v {
        int a = 4;
    }

    int b = 1;
}

function testReachableCodeWithBinaryCondition4() returns int {
    10|20 b = 10;

    if b == 10 {
        return 10;
    }

    20 c = b;

    if b  == 20 {
        return 20;
    }
}

function testReachableCodeWithBinaryCondition5() returns int {
    10 b = 10;

    if b == 10 {
        return 10;
    }
}

type Type1 10|20;

function testReachableCodeWithBinaryCondition6() returns int {
    Type1 b = 10;

    if b != 10 {
        return 20;
    }

    10 c = b;

    if b != 20 {
        return 10;
    }
}

type Type2 10;

function testReachableCodeWithBinaryCondition8() returns int {
    Type2 b = 10;

    if b == 10 {
        return 10;
    }
}

function testReachableCodeWithBinaryCondition9() returns int {
    Type1 b = 10;

    if b != 10 {
        return 20;
    }

    10 c = b;

    if b != 20 {
        return 10;
    }
}

function testReachableCodeWithUnaryCondition9() returns int {
    Type1 b = 10;

    if !(b == 10) {
        return b;
    }

    10 c = b;

    if !(c == 20) {
        return c;
    }
}

function testReachableCodeWithUnaryCondition10() returns int {
    int|string a = 10;

    if !(a is int) {
        return 1;
    }

    int b = a;
    a = 20;
    int|string c = a;

    if !(c is int) {
        return 2;
    } else {
        return 3;
    }
}

function testReachableCodeWithUnaryConditionsInIf() {
    int res = testReachableCodeWithUnaryCondition9();
    assertEqual(res, 10);

    res = testReachableCodeWithUnaryCondition10();
    assertEqual(res, 3);
}

function testReachableCodeWithTypeNarrowing() {
    int? res = getValueForToken({kind: -1, value: ()});
    assertEqual(res, ());

    res = getValueForToken2(true);
    assertEqual(res, ());
}

type Token record {
    int kind;
    anydata value;
};

function getValueForToken(Token previousToken) returns int? {
    if previousToken.kind == -1 {
        Token token = {kind: 0, value: ()};

        if token.kind != 0 {
            return 10;
        }

        if token.kind == 1 {
            token = {kind: 0, value: ()};
            return 20;
        }
    } else {
        Token token = {kind: 0, value: ()};
        return 30;
    }
    return;
}

function getValueForToken2(boolean b) returns int? {
    if b {
        Token token = {kind: 0, value: ()};

        if token.kind != 0 {
            return 10;
        }

        if token.kind == 1 {
             return 20;
        }
    } else {
        Token token = {kind: 0, value: ()};
        return 30;
    }
    return;
}

function testReachableCodeWithNonTerminatingLoop1() returns boolean? {
    int a = 10;
    while true {
        if a == 10 {
            return true;
        }
    }
}

function testReachableCodeWithNonTerminatingLoop2() returns boolean? {
    int a = 10;
    while true {
        if a == 10 {
            panic error("Error");
        }
    }
}

function testReachableCodeWithTerminatingLoop1() returns boolean? {
    int a = 10;
    while true {
        if a == 10 {
            break;
        }
    }
    return true;
}

function testReachableCodeWithTerminatingLoop2() returns boolean? {
    int a = 10;
    while true {
        if a == 10 {
            break;
        }
        return true;
    }
    return false;
}

function testTerminatingAndNonTerminatingLoops() {
    boolean? res = testReachableCodeWithNonTerminatingLoop1();
    assertEqual(res, true);

    boolean|error? res2 = trap testReachableCodeWithNonTerminatingLoop2();
    assertEqual(res2 is error, true);
    assertEqual((<error>res2).message(), "Error");

    res = testReachableCodeWithTerminatingLoop1();
    assertEqual(res, true);

    res = testReachableCodeWithTerminatingLoop2();
    assertEqual(res, false);
}

function testTypeNarrowingWithWhileNotCompletedNormally() returns int? {
    int? a = 10;
    if a is int {
        while true {
            if a == 10 {
                return a;
            }
        }
    }
    () b = a;
    return b;
}

function testTypeNarrowingWithWhileNotCompletedNormally2() returns int? {
    int? a = 10;
    if a is int {
        while true {
            return a;
        }
    }
    () b = a;
    return b;
}

function testTypeNarrowingWithWhileCompletedNormally() returns int? {
    int? a = 10;
    if a is int {
        while true {
            if a == 10 {
                break;
            }
        }
    }
    int? b = a + 10;
    return b;
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
    int? b = a + 10;
    return b;
}

function testTypeNarrowingWithDifferentWhileCompletionStatus() {
    int? res = testTypeNarrowingWithWhileNotCompletedNormally();
    assertEqual(res, 10);

    res = testTypeNarrowingWithWhileNotCompletedNormally2();
    assertEqual(res, 10);

    res = testTypeNarrowingWithWhileCompletedNormally();
    assertEqual(res, 20);

    res = testTypeNarrowingWithWhileCompletedNormally2();
    assertEqual(res, ());
}

function testTypeNarrowingFunc1() returns int? {
    int? i = 10;
    if i == () {
        return;
    }

    int j = i;
    int m = 5;

    int|string? k = 10;
    if k is int? {
        if k == () {
            return;
        }
        m = i + k + j;
    }

    return m;
}

function testTypeNarrowingFunc2(int y, int|error x) {
    if (x is error) {
        var detailMessage = x.detail()["message"];
        if (detailMessage is string) {
            if y == 0 && detailMessage != "foo" {
                panic error("Expected: foo, found: " + detailMessage);
            }
            if y != 0 && detailMessage != "int value" {
                panic error("Expected: int value, found: " + detailMessage);
            }
            panic error("Expected: string value, found: " + detailMessage);
        }
        return;
    }
    panic error("Expected an error found: " + (typeof x).toString());
}

function testTypeNarrowingFunc3(int y, int|error x) {
    if (x is error) {
        var detailMessage = x.detail()["message"];
        if (detailMessage is string) {
            if y == 0 && detailMessage !is "foo" {
                panic error("Expected: foo, found: " + detailMessage);
            }
            if y != 0 && detailMessage !is "int value" {
                panic error("Expected: int value, found: " + detailMessage);
            }
            panic error("Found: " + detailMessage);
        }
        return;
    }
    panic error("Expected an error found: " + (typeof x).toString());
}

function testTypeNarrowingFunc4(int y, int|error x) {
    if (x is error) {
        var detailMessage = x.detail()["message"];
        if (detailMessage !is string) {
            return;
        }
        if y == 0 || detailMessage != "foo" {
            panic error("Expected: foo, found: " + detailMessage);
        }
    }
}

function testTypeNarrowingFunc5(int y) returns int? {
    int|string? a = 10;
    if a is string? {
        return;
    }
    if a is int {
        if y == 10 || a == 10 {
            return a;
        }
        return a + 10;
    }
}

function testTypeNarrowingWithEqualityInCondition() {
    assertEqual(testTypeNarrowingFunc1(), 30);

    error? result = trap testTypeNarrowingFunc2(1, error("ABC", message="ABC"));
    assertEqual(true, result is error);
    error err = <error>result;
    assertEqual(err.message(), "Expected: int value, found: ABC");

    result = trap testTypeNarrowingFunc3(1, error("ABC", message="int value"));
    assertEqual(true, result is error);
    err = <error>result;
    assertEqual(err.message(), "Found: int value");

    result = trap testTypeNarrowingFunc4(0, error("ABC", message="int value"));
    assertEqual(true, result is error);
    err = <error>result;
    assertEqual(err.message(), "Expected: foo, found: int value");

    assertEqual(testTypeNarrowingFunc5(30), 10);
}

function testReachableStatementInQueryAction1() returns error? {
    check from var item in 1 ... 5
        where true
        do {
            int _ = 10;
        };
}

function testReachableStatementInQueryAction2() returns error? {
    checkpanic from var item in 1 ... 5
        where true
        do {
            int _ = 10;
        };
}

function testReachableStatementInQueryAction3() returns error? {
    error? a = from var item in 1 ... 5
        where true
        do {
            int _ = 10;
        };

    return a;
}

function testReachableStatementInQueryAction4() returns error? {
    error? a = trap from var item in 1 ... 5
        where true
        do {
            int _ = 10;
        };

    return a;
}

function testReachableStatementInQueryAction5() returns error? {
    error? a = <error?> from var item in 1 ... 5
        where true
        do {
            int _ = 10;
        };

    return a;
}

function testReachableStatementInQueryAction6() returns error? {
    return (from var item in 1 ... 5
        where true
        do {
            int _ = 10;
        });
}

function testReachableStatementInQueryAction7() returns int {
    match from var item in 1 ... 5
        where true
        do {
            int _ = 10;
        } {
        () => {
            return 1;
        }
    }
    return 2;
}

function testReachableStatementInQueryAction8() returns int {
    error? a = from int item in 1 ... 5
        where item is int
        do {
            int _ = 10;
        };

    if a is () {
        return 1;
    }
    return 2;
}

function testReachableStatementInQueryAction9() returns error? {
    error? a = ();
    check from var item in 1 ... 5
        where item < 2
        do {
            a = (from var value in 1 ... 5
                where value is int
                where value < 2
                do {
                    int _ = 10;
                });
        };

    return a;
}

function testReachableStatementInQueryAction() {
    assertEqual(true, testReachableStatementInQueryAction1() is ());
    assertEqual(true, testReachableStatementInQueryAction2() is ());
    assertEqual(true, testReachableStatementInQueryAction3() is ());
    assertEqual(true, testReachableStatementInQueryAction4() is ());
    assertEqual(true, testReachableStatementInQueryAction5() is ());
    assertEqual(true, testReachableStatementInQueryAction6() is ());
    assertEqual(1, testReachableStatementInQueryAction7());
    assertEqual(1, testReachableStatementInQueryAction8());
    assertEqual(true, testReachableStatementInQueryAction9() is ());
}

function testUnreachablePanicStmt1() {
    if true {
        return;
    } else if true {
        panic error("Error"); // OK
    }
}

function testUnreachablePanicStmt2() {
    while false {
        panic error("Error"); // OK
    }
}

function testUnreachablePanicStmt3() {
    int a = 10;
    if a !is int {
        panic error("Error"); // OK
    }
}

function testUnreachablePanicStmt4() {
    if false {
        panic error("Error"); // OK
    }
}

function testUnreachablePanicStmt5() {
    int? a = 10;
    if a is int {
    } else if a is () {
    } else {
        panic error("Error"); // OK
    }
}

function testUnreachablePanicStmt6() {
    int a = 10;
    while a !is int {
        panic error("Error"); // OK
    }
}

function testUnreachablePanicStmt7() {
    while true {
        return;
    }
    panic error("Error"); // OK
}

function testUnreachablePanicStmt8() {
    if true {
        return;
    }
    panic error("Error"); // OK
}

function testUnreachablePanicStmt9() {
    if true {
        return;
    } else {
        panic error("Error"); // OK
    }
}

function testUnreachablePanicStmt10() {
    if true {
        return;
        panic error("Error"); // OK
    }
    panic error("Error"); // OK
}

function testReachabilityWithQueryAction1() returns string {
    string[] stringArray = ["Hello", " ", "World"];

    error? unionResult = from var item in stringArray
        where item == "Hello"
        do {
            return "Hello";
        };

    if unionResult is error {
        return "ballerina";
    } else {
        return "c#";
    }
}

function testReachabilityWithQueryAction2() returns string {
    string[] stringArray = ["Hello", " ", "World"];

    error? unionResult = from var item in stringArray
        where item == "Hello"
        do {
            panic error("Panic!");
        };

    if unionResult is error {
        return "ballerina";
    } else {
        return "c#";
    }
}

function testReachabilityWithQueryAction3() returns string {
    string[] stringArray = ["Hello", " ", "World"];

    error? unionResult = from var item in stringArray
        where item == "Hello"
        do {
            while true {
                return "Hello";
            }
        };

    if unionResult is error {
        return "ballerina";
    } else {
        return "c#";
    }
}

function testReachabilityWithQueryAction() {
    assertEqual(testReachabilityWithQueryAction1(), "Hello");

    string|error res = trap testReachabilityWithQueryAction2();
    assertEqual(res is error, true);
    assertEqual((<error>res).message(), "Panic!");

    assertEqual(testReachabilityWithQueryAction3(), "Hello");
}

function assertEqual(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }

    if actual === expected {
        return;
    }

    string actualValAsString = actual.toString();
    string expectedValAsString = expected.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
