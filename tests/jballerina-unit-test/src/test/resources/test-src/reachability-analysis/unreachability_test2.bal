// Copyright (c) 2022 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function testUnreachabilityWithReturnStmt() {
    return;
    return; // unreachable code
}

function testUnreachabilityWithReturnStmt2() {
    return;
    int _ = 5; // unreachable code
    testFunction();
}

function testUnreachabilityWithReturnStmtInsideIf1() {
    if (true) {
        return;
    }

    testFunction(); // unreachable code
}

const TRUE = true;

type True true;

function testUnreachabilityWithConstAndTypeRefInsideIfStmt(int n) {
    True a = true;
    if n == 0 {
        if (a) {
            return;
        }

        testFunction(); // unreachable code
        return;
    }

    if (TRUE) {
        return;
    }

    testFunction(); // unreachable code
    return;
}

const FALSE = false;

type False false;

function testUnreachabilityWithConstAndTypeRefInsideIfStmt2(int n) {
    False a = false;
    if n == 0 {
        if (a) {
            return; // unreachable code
        }

        testFunction();
        return;
    }

    if (FALSE) {
        return; // unreachable code
    }

    return;
    testFunction(); // unreachable code
}

function testUnreachabilityWithIfElseStmt(boolean enabled) {
    if enabled {
        return;
    } else {
        return;
    }

    testFunction(); // unreachable code
}

function testUnreachabilityWithNestedIfStmt(int n) {
    if n == 0 {
        if (true) {
            return;
        } else {
            testFunction(); // unreachable code
        }

        testFunction();
        return;
    }

    if true {
        if false {
            if true { // unreachable code
                testFunction();
                string _ = "var";
            } else {
                string _ = "bar";
            }
        }
    }

    if true {
        if true {
            if true {
                testFunction();
                string _ = "var";
            } else {
                string _ = "bar"; // unreachable code
            }
        }
    }

    if true {
        if true {
            if true {
                testFunction();
                string _ = "var";
                return;
            }
            string _ = "bar"; // unreachable code
        }
    }

    if true {
        if true {
            if true {
                if true {
                    if true {
                        if false {
                        } else {
                            if true {
                                if false {
                                }
                                if true {
                                    testFunction();
                                    string _ = "var";
                                    return;
                                }
                            }
                        }
                    }
                }

            }
        }
        string _ = "bar"; // unreachable code
    }

    if true {
        if true {
            if true {
                testFunction();
                string _ = "var";
                if true {
                    if true {
                        return;
                    }
                }
            }
        }
    }

    string _ = "bar"; // unreachable code
}

function testUnreachabilityWhereTheIsExprIsConstantTrue() {
    string myString = "ballerina";
    if myString is anydata {
        testFunction();
    } else {
        string _ = "ballerina"; // unreachable code
    }
}

function testUnreachabilityWithIfStmtWithAConditionInWhichTheStaticTypeIsAUnion() {
    int|string myString = "ballerina";

    if myString is int {
        testFunction();
    } else if myString is string {
        string _ = "book";
    } else {
        if true { // unreachable code
            testFunction();
        }
        string _ = "ballerina";
    }

    if myString is int|string {
        return;
    }

    string _ = "ballerina"; // unreachable code
}

function testUnreachabilityWithIfStmtWithAConditionInWhichTheStaticTypeIsAUnion2() {
    var result = functionReturnsNilOrError();

    if result is error {
        testFunction();
    } else if result is () {
        string _ = "book";
    } else {
        if true { // unreachable code
            testFunction();
        }
        string _ = "ballerina";
    }

    if result is error? {
        testFunction();
    } else {
        string _ = "ballerina"; // unreachable code
    }
}

function testUnreachabilityWithATypeNarrowedVariableInsideIfCondition() {
    int|string a = 3;

    if a is string {
        return;
    }

    if a is int {

    } else {
        string _ = "ballerina"; // unreachable code
    }
}

function testUnreachabilityWithATypeNarrowedVariableInsideIfCondition2() {
    int|string a = 3;

    if a is string {
        return;
    }

    if a is int {
        return;
    }

    string _ = "ballerina"; // unreachable code
}

function testUnreachabilityWithATypeNarrowedVariableInsideIfCondition3() {
    int|string|boolean a = true;
    if a is int {
        return;
    } else if a is string {
        return;
    } else if a is boolean {
        return;
    } else {
        never _ = a;
    }
}

function testUnreachabilityWithATypeNarrowedVariableInsideIfCondition4() {
    int|string|boolean a = true;
    if a is int {
        return;
    }

    if a is string {
        return;
    }

    if a is boolean {
        return;
    }
    
    never _ = a;
}

function testUnreachabilityWithIfStmtWithEqualityExpr() {
    1|2 result = 2;

    if result == 1 {
        testFunction();
    } else if result == 2 {
        string _ = "book";
    } else {
        if true { // unreachable code
            testFunction();
        }
        string _ = "ballerina";
    }

    if result != 1 {
        testFunction();
    } else if result != 2 {
        string _ = "ballerina";
    } else {
        string _ = "ballerina"; // unreachable code
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr2() {
    if true == true {
        return;
    }

    string _ = "Ballerina"; // unreachable code
}

function testUnreachabilityWithIfStmtWithEqualityExpr3() {
    if true == true {
        return;
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr4() {
    if true == true {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr5() {
    if true == true {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr6() {
    if TRUE == true {
        return;
    }

    string _ = "Ballerina"; // unreachable code
}

function testUnreachabilityWithIfStmtWithEqualityExpr7() {
    if TRUE == TRUE {
        return;
    } else {
        string _ = "Ballerina";  // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr8() {
    if TRUE == FALSE {
        return; // unreachable code 
    } else if true == true {
        string _ = "Ballerina";
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr9() {
    if FALSE == FALSE {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr10() {
    if false == false {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr11() {
    True t = true;
    if t == t {
        return;
    }

    string _ = "Ballerina"; // unreachable code
}

function testUnreachabilityWithIfStmtWithEqualityExpr12() {
    True t = true;
    if t == t {
        return;
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr13() {
    False f = false;
    if true == f {
        return; // unreachable code 
    } else if true == true {
        string _ = "Ballerina";
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr14() {
    False f = false;
    if f == f {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr15() {
    True t = true;
    False f = false;

    if f == t {
        return; // unreachable code 
    } else if t == t {
        string _ = "Ballerina";
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr16() {
    if true != true {
        return; // unreachable code
    }

    string _ = "Ballerina";
}

function testUnreachabilityWithIfStmtWithEqualityExpr17() {
    if true != true {
        return; // unreachable code 
    } else {
        string _ = "Ballerina";
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr18() {
    if true != false {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr19() {
    if true != true {
        return; // unreachable code 
    } else if true == true {
        string _ = "Ballerina";
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr20() {
    if TRUE != false {
        return;
    }

    string _ = "Ballerina"; // unreachable code
}

function testUnreachabilityWithIfStmtWithEqualityExpr21() {
    if TRUE == TRUE {
        return;
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr22() {
    if TRUE == FALSE {
        return; // unreachable code 
    } else if true == true {
        string _ = "Ballerina";
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr23() {
    if FALSE == FALSE {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr24() {
    if false == false {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr25() {
    True t = true;
    if t == t {
        return;
    }

    string _ = "Ballerina"; // unreachable code
}

function testUnreachabilityWithIfStmtWithEqualityExpr26() {
    True t = true;
    if t == t {
        return;
    } else {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr27() {
    False f = false;
    if true == f {
        return; // unreachable code 
    } else if true == true {
        string _ = "Ballerina";
    }
}

function testUnreachabilityWithIfStmtWithEqualityExpr28() {
    False f = false;
    if f == f {
        return;
    } else if true == true {
        string _ = "Ballerina"; // unreachable code 
    }
}

function testUnreachabilityWithIfStmtWithLogicalExprContainingEqualityExpr() {
    1|2 result = 2;

    if result == 1 || result == 2 {
        testFunction();
    } else {
        string _ = "ballerina"; // unreachable code
    }

    if result == 1 && result == 2 {
        testFunction(); // unreachable code
    } else {
        string _ = "ballerina";
    }
}

function testUnreachabilityWithIfStmtWithLogicalExprContainingTypeTestExpr() {
    1|2 result = 2;

    if result is 1 || result is 2 {
        testFunction();
    } else {
        string _ = "ballerina"; // unreachable code
    }

    if result is 1 && result == 2 {
        testFunction(); // unreachable code
    } else {
        string _ = "ballerina";
    }
}

function testUnreachabilityWithIfStmtWithUnaryNot() {
    1|2 result = 2;

    if !(result == 1 || result == 2) {
        testFunction(); // unreachable code
    } else {
        string _ = "ballerina";
    }

    if !(result is 1 || result is 2) {
        testFunction(); // unreachable code
    } else {
        string _ = "ballerina";
    }
}

enum COLOR {
    RED,
    GREEN,
    BLUE = "blue"
}

function testUnreachabilityWithIfStmtWithAConditionUsingEnum() {
    COLOR color = "RED";

    if color is RED {

    } else if color is GREEN {

    } else if color is BLUE {

    } else {
        string _ = "ballerina"; // unreachable code
    }

    if color is RED {

    } else if true {

    } else if color is BLUE {
        string _ = "ballerina"; // unreachable code
    } else {

    }
}

function testUnreachabilityWithIfStmtWithAConditionUsingEnum2() {
    COLOR color = "RED";

    if color is RED {
        return;
    }

    if color is GREEN {
        return;
    }

    if color is BLUE {
        return;
    }

    string _ = "ballerina"; // unreachable code
}

function testUnreachabilityWithFailStmt() returns error? {
    fail error("fail msg");
    string _ = "ballerina"; // unreachable code
}

function testUnreachabilityWithFailStmtInsideIf() returns error? {
    if true {
        fail error("fail msg");
    }

    string _ = "ballerina"; // unreachable code
}

function testUnreachabilityWithFailStmtInsideIf2() returns error? {
    if true {
        fail error("fail msg");
    } else {
        string _ = "ballerina"; // unreachable code
    }

    string _ = "ballerina";
}

function testUnreachableCodeAfterPanic() {
    panic error("Timeout");
    string _ = ""; // unreachable code
}

function testUnreachableCodeAfterPanic2() {
    if true {
        panic error("Timeout");
    }

    string _ = ""; // unreachable code
}

function testUnreachableCodeAfterPanic3() {
    if true {
        panic error("Timeout");
    } else {
        string _ = ""; // unreachable code
    }

    string _ = "";
}

function testUnreachableCodeAfterPanic4() {
    trapFuc();
    string _ = ""; // unreachable code
}

function testUnreachableCodeAfterPanic5() {
    if true {
        trapFuc();
    }

    string _ = ""; // unreachable code
}

function testUnreachableCodeAfterPanic6() {
    if true {
        trapFuc();
    } else {
        string _ = ""; // unreachable code
    }

    string _ = "";
}

function testUnreachabilityWithReturnStmtInSideWhile() {
    while true {
        return;
    }

    return; // unreachable code
}

function testUnreachabilityWithReturnStmtInSideWhile2() {
    while true {
        return;
        int _ = 5; // unreachable code
        testFunction();
    }
}

function testUnreachabilityWithBreakStmtInSideWhile() {
    while true {
        break;
        int _ = 5; // unreachable code
        testFunction();
    }
}

function testUnreachabilityWitContinueStmtInSideWhile() {
    int i = 0;
    while i > 0 {
        continue;
        int _ = 5; // unreachable code
        testFunction();
    }
}

function testUnreachabilityWitContinueStmtInSideWhile2() {
    while false {
        continue; // unreachable code
        int _ = 5;
        testFunction();
    }
}

function testUnreachabilityAfterWileTrue() {
    while true {
        
    }
    testFunction();
}

function testUnreachabilityWithConstAndTypeRefInsideWhileStmt(int n) {
    True a = true;
    if n == 0 {
        while (a) {
            return;
        }

        testFunction(); // unreachable code
        return;
    }

    while TRUE {
        return;
    }

    testFunction(); // unreachable code
    return;
}

function testUnreachabilityWithConstAndTypeRefInsideWhileStmt2(int n) {
    False a = false;
    if n == 0 {
        while a {
            return; // unreachable code
        }

        testFunction();
        return;
    }

    while FALSE {
        return; // unreachable code
    }

    return;
    testFunction(); // unreachable code
}

function testUnreachabilityWithNestedWhileStmt(int n) {
    while n == 0 {
        while (true) {
            return;
        }

        testFunction(); // unreachable code
        return;
    }

    while true {
        while false {
            if true { // unreachable code
                testFunction();
                string _ = "var";
            } else {
                string _ = "bar";
            }
        }
        break;
    }

    while true {
        while true {
            while true {
                testFunction();
                string _ = "var";
            }
            string _ = "bar"; // unreachable code
        }
        break; // unreachable code
    }

    if true {
        while true {
            if true {
                continue;
            }
        }
        string _ = "bar"; // unreachable code
    }
}

function testUnreachabilityWithNestedWhileStmt2() returns string {
    int i = 4;
    TRUE t = true;
    while true {
        while true == t {
            if i != 2 {
                break;
            }
        }
    }

    return "pc" ;  // unreachable code
}

function testUnreachabilityWhereTheIsExprIsConstantTrueInsideWhile() {
    string myString = "ballerina";

    while myString is anydata {
        testFunction();
    }

    string _ = "ballerina"; // unreachable code
}

function testUnreachabilityWithWhileWithAConditionInWhichTheStaticTypeIsAUnion() {
    int|string myString = "ballerina";

    while myString is int|string {
        return;
    }

    string _ = "ballerina"; // unreachable code
}

function testUnreachabilityWithWhileWithAConditionInWhichTheStaticTypeIsAUnion2() {
    int|string myString = "ballerina";

    while myString is int {
        testFunction();
        while myString is int {
            
        }

        if true { // unreachable code
            testFunction();
        }

        string _ = "ballerina";
    }
}

function testUnreachabilityWithWhileStmtWithEqualityExpr() {
    1|2 result = 2;

    while result == 1 {
        while result == 1 {

        }

        string _ = "ballerina"; // unreachable code
    }

    while result != 1 {
        while result == 2 {

        }

        testFunction(); // unreachable code
        string _ = "ballerina";
    }

    while result != 1 {
        while result == 1 {
            if true { // unreachable code
                testFunction();
            }

            string _ = "ballerina";
        }
    }

    while result == 1 {
        while result != 2 {
            break;
            string _ = "ballerina"; // unreachable code
        }

        if true {
            break;
        }

        string _ = "ballerina"; // unreachable code
    }
}

function testUnreachabilityWithWhileStmtWithLogicalExprContainingEqualityExpr() {
    1|2 result = 2;

    while result == 1 || result == 2 {
        testFunction();
    }

    string _ = "ballerina"; // unreachable code

    while result == 1 && result == 2 {
        testFunction(); // unreachable code
    }

    string _ = "ballerina";
}

function testUnreachabilityWithWhileStmtWithLogicalExprContainingTypeTestExpr() {
    1|2 result = 2;

    while result is 1 || result is 2 {
        testFunction();
    }

    string _ = "ballerina"; // unreachable code

    while result is 1 && result == 2 {
        testFunction(); // unreachable code
    }

    string _ = "ballerina";
}

function testUnreachabilityWithWhileStmtWithUnaryNot() {
    1|2 result = 2;

    while !(result == 1 || result == 2) {
        testFunction(); // unreachable code
    }

    string _ = "ballerina";

    while !(result is 1 || result is 2) {
        testFunction(); // unreachable code
    }

    string _ = "ballerina";
}

function testUnreachabilityWithWhileStmtWithAConditionUsingEnum() {
    COLOR color = "RED";

    while color is RED|GREEN {
        while color is GREEN {
            while color !is GREEN {
                string _ = "ballerina"; // unreachable code
            }
        }
    }

    while color is GREEN|BLUE {
        if color is GREEN {

        } else if color is BLUE {

        } else {
            string _ = "ballerina"; // unreachable code
        }
    }

    while color is GREEN|BLUE {
        if color is GREEN {

        } else if color is BLUE {
            break;
        }

        string _ = "ballerina"; // unreachable code
    }

    while color is GREEN|BLUE {
        if color is GREEN {

        } else if color is BLUE {
            continue;
        }

        string _ = "ballerina"; // unreachable code
    }
}

function testUnreachabilityWithFailStmtInsideWhile() returns error? {
    while true {
        fail error("fail msg");
    }

    string _ = "ballerina"; // unreachable code
}

function testUnreachabilityWithFailStmtInsideWhile2() returns error? {
    while true {
        fail error("fail msg");
    }

    string _ = "ballerina"; // unreachable code
    string _ = "ballerina";
}

function testUnreachableCodeAfterPanicInsideWhile() {
    while true {
        panic error("Timeout");
    }

    string _ = ""; // unreachable code
}

function testUnreachableCodeAfterPanicInsideWhile2() {
    while true {
        trapFuc();
    }

    string _ = ""; // unreachable code
}

public function testUnreachabilityWithQueryAction() {
    string[] stringArray = ["Hello", " ", "World"];

    error? unionResult = from var item in stringArray
        where item == "Hello"
        do {
            if item != "Hello" {
                string _ = "ballerina"; // unreachable code
            }
        };

    if unionResult is () {
        return;
    }

    panic unionResult;
}

public function testUnreachabilityWithQueryAction2() {
    string[] stringArray = ["Hello", " ", "World"];

    error? unionResult = from var item in stringArray
        where item == "Hello"
        do {
            if item == "Hello" {
                return;
            }
            string _ = "ballerina"; // unreachable code
        };

    if unionResult is () {
        return;
    }

    panic unionResult;
}

public function testUnreachabilityWithQueryAction3() {
    string[] stringArray = ["Hello", " ", "World"];

    error? unionResult = from var item in stringArray
        where item == "Hello"
        do {
            if item == "Hello" {

            } else {
                string _ = "ballerina"; // unreachable code
            }
        };

    if unionResult is () {
        return;
    }

    panic unionResult;
}

public function testUnreachabilityWithQueryAction4() {
    string[] stringArray = ["Hello", " ", "World"];

    error? unionResult = from var item in stringArray
        where item == "Hello"
        do {
            while item == "Hello" {

            }

            string _ = "ballerina"; // unreachable code
        };

    if unionResult is () {
        return;
    }

    panic unionResult;
}

function testUnreachabilityInsideMatchBlockWithIfStmt() {
    int? x = ();
    match x {
        var a if a is int => {
            if a !is int {
                string _ = "ballerina"; // unreachable code
            }
        }
    }
}

function testUnreachabilityInsideMatchBlockWithWhileStmt() {
    int? x = ();
    match x {
        var a if a is int => {
            while a !is int {
                string _ = "ballerina"; // unreachable code
            }
        }
    }
}

// TODO: fix #34579 and enable the below test case
// function testUnreachabilityWithDifferentExpressionsAsIfCondition() {
//     boolean a = false;
//     if <false>a {
//         string _ = "ballerina";
//     }

//     if functionReturnsFalse() {
//         string _ = "ballerina";
//     }

//     Myrecord myRec = {boolFalse: false};
//     if myRec.boolFalse {
//         string _ = "ballerina";
//     }

//     function () returns false myfunc = () => false;

//     if myfunc() {
//         string _ = "ballerina";
//     }

//     MyClass myObj = new;
//     if myObj.boolFalse {
//         string _ = "ballerina";
//     }

//     if myObj.functionReturnsFalse() {
//         string _ = "ballerina";
//     }

//     true[] trueArray = [true];
//     if trueArray[0] {
//         string _ = "ballerina";
//     }

// }

function testUnreachabilityAfterTerminatedIfBlock1(int a) returns int {
    if a > 10 {
        return a + 1;
    } else if a > 12 {
        return a - 1;
    } else {
        return -a;
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedIfBlock2(int a) returns int {
    if a > 10 {
        if a > 12 {
            return a;
        }
        return a - 1;
    } else {
        return -a;
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedIfBlock3(int a) returns int {
    if a > 10 {
        return a;
    }
    return -a;
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedMatchBlock1(int a) returns int {
    match a {
        1 => {
            return a + 1;
        }
        _ => {
            return a - 1;
        }
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedMatchBlock2(int a) returns int {
    match a {
        var x if a > 10 => {
            if x % 2 == 0 {
                return a * 2;
            }
            return x + 1;
        }
        _ => {
            return a - 1;
        }
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedMatchBlock3(int a) returns int {
    match a {
        2|3 => {
            check functionReturnsNilOrError();
            return a + 1;
        }
        _ => {
            return a - 1;
        }
    } on fail {
    	return -1;
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedDoBlock1() returns int {
    do {
        return 10;
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedDoBlock2(int a) returns int {
    do {
        if a > 10 {
            if a > 12 {
                return a + 1;
            }
            return a - 1;
        } else {
            return -a;
        }
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedDoBlock3(int a) returns int {
    do {
        check functionReturnsNilOrError();
        return 10;
    } on fail {
        return -1;
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedWhileBlock1() returns int {
    while true {
        
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedWhileBlock2() returns int {
    while true {
        int|string a = 12;
        if a is int {
            return -1;
        }
        string _ = a;
        panic error("Error");
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedLockBlock1(int a) returns int {
    lock {
        return 1;
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedLockBlock2(int a) returns int {
    lock {
        do {
            if a > 10 {
                if a > 12 {
                    check functionReturnsNilOrError();
                    return 10;
                }
                return a - 1;
            } else {
                return -a;
            }
        } on fail {
            return -1;
        }
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedBlock1() returns int {
    {
        return 1;
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedBlock2() returns int {
    {
        int|string a = 12;
        if a is int {
            return -1;
        }
        string _ = a;
        panic error("Error");
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function testUnreachabilityAfterTerminatedBlock3(int a) returns int {
    if a > 10 {
        return a;
    }
    {
        return -a;
    }
    int _ = 1; // unreachable code
    int _ = 2;
}

function trapFuc() returns never {
    panic error("Timeout");
}

function functionReturnsFalse() returns false => false;

type Myrecord record {|
    false boolFalse;
|};

class MyClass {
    false boolFalse = false;

    function functionReturnsFalse() returns false {
        return false;
    }
}

function testFunction() {
}

function functionReturnsNilOrError() returns error? {
}
