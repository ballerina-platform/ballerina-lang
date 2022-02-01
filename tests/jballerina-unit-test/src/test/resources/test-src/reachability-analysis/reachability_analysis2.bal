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

function executeTestReachabilityWithIfContainingReturn() {
    assertEqual(testReachabilityWithIfContainingReturn(), "Ballerina");
    assertEqual(testReachabilityWithIfContainingReturn2(), "Ballerina");
    assertEqual(testReachabilityWithIfContainingReturn3(), "Hello");
    assertEqual(testReachabilityWithIfContainingReturn4(), "Hello");
    assertEqual(testReachabilityWithIfContainingReturn5(), "Ballerina");
    assertEqual(testReachabilityWithIfContainingReturn6(), "Ballerina");
    assertEqual(testReachabilityWithIfContainingReturn7(), "Ballerina");
}

function testReachabilityWithIfContainingReturn() returns string {
    boolean flag = false;
    if flag {
        return "Hello";
    }

    return "Ballerina";
}

function testReachabilityWithIfContainingReturn2() returns string {
    boolean flag = false;
    if flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfContainingReturn3() returns string {
    boolean flag = true;
    if true == flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfContainingReturn4() returns string {
    boolean flag = true;
    if flag == true {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfContainingReturn5() returns string {
    boolean flag = true;
    if true != flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfContainingReturn6() returns string {
    boolean flag = true;
    if flag != true {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfContainingReturn7() returns string {
    boolean flag = false;
    if true {
        if true {
            if flag {
                return "Hello";
            }
        }
    }

    return "Ballerina";
}

function executeTestReachabilityWithIfUsingConstInExpr() {
    assertEqual(testReachabilityWithIfUsingConstInExpr1(), "Hello");
    assertEqual(testReachabilityWithIfUsingConstInExpr2(), "Hello");
    assertEqual(testReachabilityWithIfUsingConstInExpr3(), "Ballerina");
    assertEqual(testReachabilityWithIfUsingConstInExpr4(), "Ballerina");
    assertEqual(testReachabilityWithIfUsingConstInExpr5(), "Ballerina");
}

const TRUE = true;

function testReachabilityWithIfUsingConstInExpr1() returns string {
    boolean flag = true;
    if TRUE == flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfUsingConstInExpr2() returns string {
    boolean flag = true;
    if flag == TRUE {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfUsingConstInExpr3() returns string {
    boolean flag = true;
    if TRUE != flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfUsingConstInExpr4() returns string {
    boolean flag = true;
    if flag != TRUE {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function testReachabilityWithIfUsingConstInExpr5() returns string {
    if true {
        if true {
            if true {

            }
        }
    }

    return "Ballerina";
}

// TODO: fix issue #34820
// function testReachablilityWithElseIf() returns string {
//     if true {
//         return "car";
//     } else if true {

//     }
// }

function executeTestReachabilityWithNestedIfContainingReturn() {
    assertEqual(testReachabilityWithNestedIfContainingReturn(), "car");
}

function testReachabilityWithNestedIfContainingReturn() returns string {
    boolean flag = true;
    
    if true {
        if flag {
            if !flag {
                if true {
                    if true {
                        if false {

                        } else {
                            if true {
                                if false {
                                }
                                string _ = "var";
                            }
                        }
                    }
                }
                return "ear";
            }
            return "car";
        }
    }
    return "bar";
}

function executeTestReachabilityWithIsExprNotConstantTrue() {
    assertEqual(testReachabilityWithIsExprNotConstantTrue(), "ballerina");
    assertEqual(testReachabilityWithIsExprNotConstantTrue(), "ballerina");
    assertEqual(testReachabilityWithIsExprNotConstantTrue(), "ballerina");
    assertEqual(testReachabilityWithIsExprNotConstantTrue(), "ballerina");
    assertEqual(testReachabilityWithIsExprNotConstantTrue2(), "java");
    assertEqual(testReachabilityWithIsExprNotConstantTrue3(), "java");
    assertEqual(testReachabilityWithIsExprNotConstantTrue4(), "python");
    assertEqual(testReachabilityWithIsExprNotConstantTrue5(), "other");
    assertEqual(testReachabilityWithIsExprNotConstantTrue6(), "C#");
}

function testReachabilityWithIsExprNotConstantTrue() returns string {
    string myString = "ballerina";
    if myString is "ballerina" {
        return "ballerina";
    } else {
        return "java";
    }
}

function testReachabilityWithIsExprNotConstantTrue2() returns string {
    string myString = "other";
    if myString is "ballerina" {
        return "ballerina";
    } else {
        return "java";
    }
}

function testReachabilityWithIsExprNotConstantTrue3() returns string {
    string myString = "java";
    if myString is "ballerina" {
        return "ballerina";
    } else if myString is "java" {
        return "java";
    }

    return "python";
}

function testReachabilityWithIsExprNotConstantTrue4() returns string {
    string myString = "other";
    if myString is "ballerina" {
        return "ballerina";
    } else if myString is "java" {
        return "java";
    }

    return "python";
}

function testReachabilityWithIsExprNotConstantTrue5() returns string {
    string myString = "other";
    if myString is "ballerina" {
        return "ballerina";
    } else if myString is "java" {
        return "java";
    } else if myString is "rust" {
        myString = "rust";
    } else {
        return "other";
    }

    return myString;
}

function testReachabilityWithIsExprNotConstantTrue6() returns string {
    string myString = "other";
    if myString is "ballerina" {
        return "ballerina";
    } else if myString is "java" {
        return "java";
    } else if myString is "rust" {
        return "rust";
    } else {
        myString = "C#";
    }

    return myString;
}

function executeTestReachabilityWithFailExpr() {
    assertEqual((<error>testReachabilityWithFailExpr()).message(), "errormsg");
    assertEqual((<error>testReachabilityWithFailExpr2()).message(), "errormsg");
    assertEqual((<error>testReachabilityWithFailExpr3()).message(), "errormsg2");
    assertEqual(testReachabilityWithFailExpr4(), "python");
    assertEqual((<error>testReachabilityWithFailExpr5()).message(), "errormsg");
    assertEqual(testReachabilityWithFailExpr6(), "C#");
    assertEqual((<error>testReachabilityWithFailExpr7()).message(), "msg");
}

function testReachabilityWithFailExpr() returns string|error {
    string myString = "ballerina";
    if myString is "ballerina" {
        fail error("errormsg");
    } else {
        return "java";
    }
}

function testReachabilityWithFailExpr2() returns string|error {
    string myString = "other";
    if myString is "ballerina" {
        return "ballerina";
    } else {
        fail error("errormsg");
    }
}

function testReachabilityWithFailExpr3() returns string|error {
    string myString = "java";
    if myString is "ballerina" {
        fail error("errormsg1");
    } else if myString is "java" {
        fail error("errormsg2");
    }

    return "python";
}

function testReachabilityWithFailExpr4() returns string|error {
    string myString = "other";
    if myString is "ballerina" {
        fail error("errormsg1");
    } else if myString is "java" {
        fail error("errormsg2");
    }

    return "python";
}

function testReachabilityWithFailExpr5() returns string|error {
    string myString = "other";
    if myString is "ballerina" {
        fail error("ballerina");
    } else if myString is "java" {
        fail error("java");
    } else if myString is "rust" {
        myString = "rust";
    } else {
        fail error("errormsg");
    }

    return myString;
}

function testReachabilityWithFailExpr6() returns string|error {
    string myString = "other";
    if myString is "ballerina" {
        fail error("ballerina");
    } else if myString is "java" {
        fail error("java");
    } else if myString is "rust" {
        fail error("rust");
    } else {
        myString = "C#";
    }

    return myString;
}

function testReachabilityWithFailExpr7() returns string|error {
    string stringVar = "boo";

    if stringVar == "boo" {
        fail error("msg");
    }

    return stringVar;
}

function executeTestReachabilityWithResetTypeNarrowing() {
    assertEqual(testReachabilityWithResetTypeNarrowing(), "car");
    assertEqual(testReachabilityWithResetTypeNarrowing2(), "car");
    assertEqual(testReachabilityWithResetTypeNarrowing3(), "jeep");
    assertEqual(testReachabilityWithResetTypeNarrowing4(), "jeep");
    assertEqual(testReachabilityWithResetTypeNarrowing5(), "van");
    assertEqual(testReachabilityWithResetTypeNarrowing6(), "van");
    assertEqual(testReachabilityWithResetTypeNarrowing7(), "jeep");
    assertEqual(testReachabilityWithResetTypeNarrowing8(), "jeep");
    assertEqual(testReachabilityWithResetTypeNarrowing9(), "jeep");
    assertEqual(testReachabilityWithResetTypeNarrowing10(), "van");
    assertEqual(testReachabilityWithResetTypeNarrowing11(), "jeep");
    assertEqual(testReachabilityWithResetTypeNarrowing12(), "jeep");
    assertEqual(testReachabilityWithResetTypeNarrowing13(), "car");
    assertEqual(testReachabilityWithResetTypeNarrowing14(), "car");
    assertEqual(testReachabilityWithResetTypeNarrowing15(), "car");
    assertEqual(testReachabilityWithResetTypeNarrowing16(), "car");
}

function testReachabilityWithResetTypeNarrowing() returns string {
    int|string myString = "other";
    if myString is string {
        myString = "restTypeNarrowing";

        if myString is string {
            return "car";
        } else {
            return "van";
        }
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing2() returns string {
    int|string myString = "other";
    if myString is string {
        myString = "restTypeNarrowing";

        if myString is string {
            return "car";
        }

        return "van";
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing3() returns string {
    int|string myString = 23;
    if myString is string {
        myString = "restTypeNarrowing";

        if myString is string {
            return "car";
        } else {
            return "van";
        }
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing4() returns string {
    int|string myString = 23;
    if myString is string {
        myString = "restTypeNarrowing";

        if myString is string {
            return "car";
        }

        return "van";
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing5() returns string {
    int|string myString = "other";
    if myString is string {
        myString = 1;

        if myString is string {
            return "car";
        } else {
            return "van";
        }
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing6() returns string {
    int|string myString = "other";
    if myString is string {
        myString = 2;

        if myString is string {
            return "car";
        }

        return "van";
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing7() returns string {
    int|string myString = 23;
    if myString is string {
        myString = 3;

        if myString is string {
            return "car";
        } else {
            return "van";
        }
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing8() returns string {
    int|string myString = 23;
    if myString is string {
        myString = 4;

        if myString is string {
            return "car";
        }

        return "van";
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing9() returns string {
    int|string myString = "other";
    if myString !is string {
        myString = "restTypeNarrowing";

        if myString is string {
            return "car";
        } else {
            return "van";
        }
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing10() returns string {
    int|string myString = "other";
    if myString is string {
        myString = "restTypeNarrowing";

        if myString !is string {
            return "car";
        }

        return "van";
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing11() returns string {
    int|string myString = 23;
    if myString is string {
        myString = "restTypeNarrowing";

        if myString !is string {
            return "car";
        } else {
            return "van";
        }
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing12() returns string {
    int|string myString = 23;
    if myString is string {
        myString = "restTypeNarrowing";

        if myString !is string {
            return "car";
        }

        return "van";
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing13() returns string {
    int|string myString = "other";
    if myString is string {
        myString = 1;

        if myString !is string {
            return "car";
        } else {
            return "van";
        }
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing14() returns string {
    int|string myString = "other";
    if myString is string {
        myString = 2;

        if myString !is string {
            return "car";
        }

        return "van";
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing15() returns string {
    int|string myString = "other";
    if myString is string {
        myString = 3;

        if myString !is string {
            return "car";
        } else {
            return "van";
        }
    } else {
        return "jeep";
    }
}

function testReachabilityWithResetTypeNarrowing16() returns string {
    int|string myString = "other";
    if myString is string {
        myString = 4;

        if myString !is string {
            return "car";
        }

        return "van";
    } else {
        return "jeep";
    }
}

function executeTestReachabilityWithIfStmtWithLogicalExprContainingEqualityExpr() {
    assertEqual(testReachabilityWithIfStmtWithLogicalExprContainingEqualityExpr(), "bar");
}

function testReachabilityWithIfStmtWithLogicalExprContainingEqualityExpr() returns string {
    1|2 result = 2;

    if result == 1 || false {
        return "foo";
    } else {
        return "bar";
    }
}

function executeTestReachabilityWithIfStmtWithAConditionUsingEnum() {
    assertEqual(testReachabilityWithIfStmtWithAConditionUsingEnum(), "red");
    assertEqual(testReachabilityWithIfStmtWithAConditionUsingEnum2(), "blue");
    assertEqual(testReachabilityWithIfStmtWithAConditionUsingEnum3(), "blue");
    assertEqual(testReachabilityWithIfStmtWithAConditionUsingEnum4(), "green");
    assertEqual(testReachabilityWithIfStmtWithAConditionUsingEnum5(), "blue");
}

enum COLOR {
    RED,
    GREEN,
    BLUE = "blue"
}

function testReachabilityWithIfStmtWithAConditionUsingEnum() returns string {
    COLOR color = "RED";

    if color is RED {
        return "red";
    } else if color is GREEN {
        return "green";
    } else if color is BLUE {
        return "blue";
    }
}

function testReachabilityWithIfStmtWithAConditionUsingEnum2() returns string {
    COLOR color = "blue";

    if color is RED {
        return "red";
    } else if color is GREEN {
        return "green";
    } else {
        return "blue";
    }
}

function testReachabilityWithIfStmtWithAConditionUsingEnum3() returns string {
    COLOR color = "blue";

    if color is RED {
        return "red";
    } else if color is GREEN {
        return "green";
    }

    return "blue";
}

function testReachabilityWithIfStmtWithAConditionUsingEnum4() returns string {
    COLOR color = "GREEN";

    if color is RED {
        return "red";
    }

    if color is BLUE {
        return "blue";
    }

    if color is GREEN {
        return "green";
    }
}

function testReachabilityWithIfStmtWithAConditionUsingEnum5() returns string {
    COLOR color = "blue";

    if color is RED {
        return "red";
    }

    if color is GREEN {
        return "green";
    }

    return "blue";

}

function executeTestReachabilityWithWhile() {
    assertEqual(testReachabilityWithWhile(), "pc");
    assertEqual(testReachabilityWithWhile2(), "pc");
    assertEqual(testReachabilityWithWhile3(), "pc");
    assertEqual(testReachabilityWithWhile4(), 1);
    assertEqual(testReachabilityWithWhile5(), 2);
    assertEqual(testReachabilityWithWhile6(), 1);
    assertEqual(testReachabilityWithNestedWhile(), "pc");
}

function testReachabilityWithWhile() returns string {
    while true {
        break;
    }

    return "pc";
}

function testReachabilityWithWhile2() returns string {
    while true {
        if true {
            break;
        }
    }

    return "pc";
}

function testReachabilityWithWhile3() returns string {
    while true {
        if true {
            break;
        }
    }

    return "pc";
}

function testReachabilityWithWhile4() returns int {
    int i = 0;
    while true {
        if i == 1 {
            break;
        }
        i += 1;
        continue;
    }

    return i;
}

function testReachabilityWithWhile5() returns int {
    2 i = 2;
    while true {
        if i == 2 {
            break;
        }
    }

    return i;
}

function testReachabilityWithWhile6() returns int {
    1 i = 1;
    while true {
        if i != 2 {
            break;
        }
    }

    return i;
}

function testReachabilityWithNestedWhile() returns string {
    int i = 4;
    TRUE t = true;
    while true {
        while true == t {
            if i != 2 {
                break;
            }
        }
        break;
    }

    return "pc";
}

function executeTestReachabilityWhereTheIsExprInsideWhile() {
    assertEqual(testReachabilityWhereTheIsExprInsideWhile(), "ballerina");
    assertEqual(testReachabilityWhereTheIsExprInsideWhile2(), "VSCode");
}

function testReachabilityWhereTheIsExprInsideWhile() returns string {
    string? myString = ();

    while myString is string {

    }

    return "ballerina";
}

function testReachabilityWhereTheIsExprInsideWhile2() returns string {
    string? myString = "VSCode";

    while myString is string {
        return myString;
    }

    return "ballerina";
}

function executeTestReachabilityWithWhileStmtWithEqualityExpr() {
    assertEqual(testReachabilityWithWhileStmtWithEqualityExpr(), "lap");
    assertEqual(testReachabilityWithWhileStmtWithEqualityExpr2(), "phone2");
    assertEqual(testReachabilityWithWhileStmtWithEqualityExpr3(), "phone3");
    assertEqual(testReachabilityWithWhileStmtWithEqualityExpr4(), 1);
}

function testReachabilityWithWhileStmtWithEqualityExpr() returns string {
    1|2 result = 2;

    while result == 1 {
        result = 2;
        while result == 1 {

        }

        return "phone";
    }

    return "lap";
}

function testReachabilityWithWhileStmtWithEqualityExpr2() returns string {
    1|2 result = 2;

    while result != 1 {
        while result != 2 {

        }

        return "phone2";
    }

    return "lap2";
}

function testReachabilityWithWhileStmtWithEqualityExpr3() returns string {
    1|2 result = 2;

    while result != 1 {
        result = 1;
        while result == 1 {
            if true {

            }

            return "phone3";
        }
    }
    return "lap3";
}

function testReachabilityWithWhileStmtWithEqualityExpr4() returns int {
    1|2 result = 2;

    while result != 1 {
        result = 2;
        while result == 2 {
            result = 1;
        }

        string _ = "ballerina";
    }

    return result;
}

function executeTestReachabilityWithWhileStmtWithAConditionUsingEnum() {
    assertEqual(testReachabilityWithWhileStmtWithAConditionUsingEnum(), "blue");
    assertEqual(testReachabilityWithWhileStmtWithAConditionUsingEnum2(), "blue");
}

function testReachabilityWithWhileStmtWithAConditionUsingEnum() returns string {
    COLOR color = "RED";

    while color is RED|GREEN {
        while color is RED {
            color = "GREEN";

            while color !is GREEN {
                return "red";
            }

            break;
        }

        break;
    }

    return "blue";
}

function testReachabilityWithWhileStmtWithAConditionUsingEnum2() returns string {
    COLOR color = "GREEN";

    while color is RED|GREEN {
        if color is GREEN {
            color = "RED";

            while color !is RED {
                return "red";
            }
        }

        break;
    }

    return "blue";
}

function executeTestReachabilityWithQueryAction() {
    //assertEqual(testReachabilityWithQueryAction(), "Hello");
    //assertEqual(testReachabilityWithQueryAction2(), "Hello");
}

// TODO: fix issue #34916
//function testReachabilityWithQueryAction() returns string {
//    string[] stringArray = ["Hello", " ", "World"];
//
//    error? unionResult = from var item in stringArray
//        where item is string
//        do {
//            if item == "Hello" {
//                return "Hello";
//            }
//        };
//
//    if unionResult is error {
//        return "ballerina";
//    } else {
//        return "c#";
//    }
//}

// TODO: fix issue #34914
//function testReachabilityWithQueryAction2() returns string {
//    string[] stringArray = ["Hello", " ", "World"];
//
//    error? unionResult = from var item in stringArray
//        where item == "Hello"
//        do {
//            return "Hello";
//        };
//
//    if unionResult is error {
//        return "ballerina";
//    } else {
//        return "c#";
//    }
//}

function executeTestReachabilityAfterCheck() {
    assertEqual(testReachabilityAfterCheck(), "string");
}

function testReachabilityAfterCheck() returns string|error {
    string stringResult = check funtionReturnsStringOrError();
    return stringResult;
}

function executeTestReachabilityAfterCheckPanic() {
    assertEqual(testReachabilityAfterCheckpanic(), "string");
}

function testReachabilityAfterCheckpanic() returns string|error {
    string stringResult = checkpanic funtionReturnsStringOrError();
    return stringResult;
}

function funtionReturnsStringOrError() returns string|error {
    return "string";
}

function assertEqual(anydata|error actual, anydata|error expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }

    if actual === expected {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(string `Assertion error: expected ${expectedValAsString} found ${actualValAsString}`);
}
