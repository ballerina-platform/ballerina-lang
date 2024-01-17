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

function checkReachabilityWithIfContainingReturn() returns string {
    boolean flag = false;
    if flag {
        return "Hello";
    }

    return "Ballerina";
}

function checkReachabilityWithIfContainingReturn2() returns string {
    boolean flag = false;
    if flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfContainingReturn3() returns string {
    boolean flag = true;
    if true == flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfContainingReturn4() returns string {
    boolean flag = true;
    if flag == true {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfContainingReturn5() returns string {
    boolean flag = true;
    if true != flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfContainingReturn6() returns string {
    boolean flag = true;
    if flag != true {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfContainingReturn7() returns string {
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

const TRUE = true;

function checkReachabilityWithIfUsingConstInExpr1() returns string {
    boolean flag = true;
    if TRUE == flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfUsingConstInExpr2() returns string {
    boolean flag = true;
    if flag == TRUE {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfUsingConstInExpr3() returns string {
    boolean flag = true;
    if TRUE != flag {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfUsingConstInExpr4() returns string {
    boolean flag = true;
    if flag != TRUE {
        return "Hello";
    } else {
        int _ = 4;
    }

    return "Ballerina";
}

function checkReachabilityWithIfUsingConstInExpr5() returns string {
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

function checkReachabilityWithNestedIfContainingReturn() returns string {
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

function checkReachabilityWithIsExprNotConstantTrue() returns string {
    string myString = "ballerina";
    if myString is "ballerina" {
        return "ballerina";
    } else {
        return "java";
    }
}

function checkReachabilityWithIsExprNotConstantTrue2() returns string {
    string myString = "other";
    if myString is "ballerina" {
        return "ballerina";
    } else {
        return "java";
    }
}

function checkReachabilityWithIsExprNotConstantTrue3() returns string {
    string myString = "java";
    if myString is "ballerina" {
        return "ballerina";
    } else if myString is "java" {
        return "java";
    }

    return "python";
}

function checkReachabilityWithIsExprNotConstantTrue4() returns string {
    string myString = "other";
    if myString is "ballerina" {
        return "ballerina";
    } else if myString is "java" {
        return "java";
    }

    return "python";
}

function checkReachabilityWithIsExprNotConstantTrue5() returns string {
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

function checkReachabilityWithIsExprNotConstantTrue6() returns string {
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

function checkReachabilityWithFailExpr() returns string|error {
    string myString = "ballerina";
    if myString is "ballerina" {
        fail error("errormsg");
    } else {
        return "java";
    }
}

function checkReachabilityWithFailExpr2() returns string|error {
    string myString = "other";
    if myString is "ballerina" {
        return "ballerina";
    } else {
        fail error("errormsg");
    }
}

function checkReachabilityWithFailExpr3() returns string|error {
    string myString = "java";
    if myString is "ballerina" {
        fail error("errormsg1");
    } else if myString is "java" {
        fail error("errormsg2");
    }

    return "python";
}

function checkReachabilityWithFailExpr4() returns string|error {
    string myString = "other";
    if myString is "ballerina" {
        fail error("errormsg1");
    } else if myString is "java" {
        fail error("errormsg2");
    }

    return "python";
}

function checkReachabilityWithFailExpr5() returns string|error {
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

function checkReachabilityWithFailExpr6() returns string|error {
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

function checkReachabilityWithFailExpr7() returns string|error {
    string stringVar = "boo";

    if stringVar == "boo" {
        fail error("msg");
    }

    return stringVar;
}

function checkReachabilityWithResetTypeNarrowing() returns string {
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

function checkReachabilityWithResetTypeNarrowing2() returns string {
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

function checkReachabilityWithResetTypeNarrowing3() returns string {
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

function checkReachabilityWithResetTypeNarrowing4() returns string {
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

function checkReachabilityWithResetTypeNarrowing5() returns string {
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

function checkReachabilityWithResetTypeNarrowing6() returns string {
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

function checkReachabilityWithResetTypeNarrowing7() returns string {
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

function checkReachabilityWithResetTypeNarrowing8() returns string {
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

function checkReachabilityWithResetTypeNarrowing9() returns string {
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

function checkReachabilityWithResetTypeNarrowing10() returns string {
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

function checkReachabilityWithResetTypeNarrowing11() returns string {
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

function checkReachabilityWithResetTypeNarrowing12() returns string {
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

function checkReachabilityWithResetTypeNarrowing13() returns string {
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

function checkReachabilityWithResetTypeNarrowing14() returns string {
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

function checkReachabilityWithResetTypeNarrowing15() returns string {
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

function checkReachabilityWithResetTypeNarrowing16() returns string {
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

function checkReachabilityWithIfStmtWithLogicalExprContainingEqualityExpr() returns string {
    1|2 result = 2;

    if result == 1 || false {
        return "foo";
    } else {
        return "bar";
    }
}

enum COLOR {
    RED,
    GREEN,
    BLUE = "blue"
}

function checkReachabilityWithIfStmtWithAConditionUsingEnum() returns string {
    COLOR color = "RED";

    if color is RED {
        return "red";
    } else if color is GREEN {
        return "green";
    } else if color is BLUE {
        return "blue";
    }
}

function checkReachabilityWithIfStmtWithAConditionUsingEnum2() returns string {
    COLOR color = "blue";

    if color is RED {
        return "red";
    } else if color is GREEN {
        return "green";
    } else {
        return "blue";
    }
}

function checkReachabilityWithIfStmtWithAConditionUsingEnum3() returns string {
    COLOR color = "blue";

    if color is RED {
        return "red";
    } else if color is GREEN {
        return "green";
    }

    return "blue";
}

function checkReachabilityWithIfStmtWithAConditionUsingEnum4() returns string {
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

function checkReachabilityWithIfStmtWithAConditionUsingEnum5() returns string {
    COLOR color = "blue";

    if color is RED {
        return "red";
    }

    if color is GREEN {
        return "green";
    }

    return "blue";

}

function checkReachabilityWithWhile() returns string {
    while true {
        break;
    }

    return "pc";
}

function checkReachabilityWithWhile2() returns string {
    while true {
        if true {
            break;
        }
    }

    return "pc";
}

function checkReachabilityWithWhile3() returns string {
    while true {
        if true {
            break;
        }
    }

    return "pc";
}

function checkReachabilityWithWhile4() returns int {
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

function checkReachabilityWithWhile5() returns int {
    2 i = 2;
    while true {
        if i == 2 {
            break;
        }
    }

    return i;
}

function checkReachabilityWithWhile6() returns int {
    1 i = 1;
    while true {
        if i != 2 {
            break;
        }
    }

    return i;
}

function checkReachabilityWithNestedWhile() returns string {
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

function checkReachabilityWhereTheIsExprInsideWhile() returns string {
    string? myString = ();

    while myString is string {

    }

    return "ballerina";
}

function checkReachabilityWhereTheIsExprInsideWhile2() returns string {
    string? myString = "VSCode";

    while myString is string {
        return myString;
    }

    return "ballerina";
}

function checkReachabilityWithWhileStmtWithEqualityExpr() returns string {
    1|2 result = 2;

    while result == 1 {
        result = 2;
        while result == 1 {

        }

        return "phone";
    }

    return "lap";
}

function checkReachabilityWithWhileStmtWithEqualityExpr2() returns string {
    1|2 result = 2;

    while result != 1 {
        while result != 2 {

        }

        return "phone2";
    }

    return "lap2";
}

function checkReachabilityWithWhileStmtWithEqualityExpr3() returns string {
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

function checkReachabilityWithWhileStmtWithEqualityExpr4() returns int {
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

function checkReachabilityWithWhileStmtWithAConditionUsingEnum() returns string {
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

function checkReachabilityWithWhileStmtWithAConditionUsingEnum2() returns string {
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

// TODO: fix issue #34916
//function checkReachabilityWithQueryAction() returns string {
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
//function checkReachabilityWithQueryAction2() returns string {
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

function checkReachabilityAfterCheck() returns string|error {
    string stringResult = check funtionReturnsStringOrError();
    return stringResult;
}

function checkReachabilityAfterCheckpanic() returns string|error {
    string stringResult = checkpanic funtionReturnsStringOrError();
    return stringResult;
}

function testReachabilityWithIfContainingReturn() {
    assertEqual(checkReachabilityWithIfContainingReturn(), "Ballerina");
}

function testReachabilityWithIfContainingReturn2() {
    assertEqual(checkReachabilityWithIfContainingReturn2(), "Ballerina");
}

function testReachabilityWithIfContainingReturn3() {
    assertEqual(checkReachabilityWithIfContainingReturn3(), "Hello");
}

function testReachabilityWithIfContainingReturn4() {
    assertEqual(checkReachabilityWithIfContainingReturn4(), "Hello");
}

function testReachabilityWithIfContainingReturn5() {
    assertEqual(checkReachabilityWithIfContainingReturn5(), "Ballerina");
}

function testReachabilityWithIfContainingReturn6() {
    assertEqual(checkReachabilityWithIfContainingReturn6(), "Ballerina");
}

function testReachabilityWithIfContainingReturn7() {
    assertEqual(checkReachabilityWithIfContainingReturn7(), "Ballerina");
}

function testReachabilityWithIfUsingConstInExpr1() {
    assertEqual(checkReachabilityWithIfUsingConstInExpr1(), "Hello");
}

function testReachabilityWithIfUsingConstInExpr2() {
    assertEqual(checkReachabilityWithIfUsingConstInExpr2(), "Hello");
}

function testReachabilityWithIfUsingConstInExpr3() {
    assertEqual(checkReachabilityWithIfUsingConstInExpr3(), "Ballerina");
}

function testReachabilityWithIfUsingConstInExpr4() {
    assertEqual(checkReachabilityWithIfUsingConstInExpr4(), "Ballerina");
}

function testReachabilityWithIfUsingConstInExpr5() {
    assertEqual(checkReachabilityWithIfUsingConstInExpr5(), "Ballerina");
}

function testReachabilityWithNestedIfContainingReturn() {
    assertEqual(checkReachabilityWithNestedIfContainingReturn(), "car");
}

function testReachabilityWithIsExprNotConstantTrue() {
    assertEqual(checkReachabilityWithIsExprNotConstantTrue(), "ballerina");
}

function testReachabilityWithIsExprNotConstantTrue2() {
    assertEqual(checkReachabilityWithIsExprNotConstantTrue2(), "java");
}

function testReachabilityWithIsExprNotConstantTrue3() {
    assertEqual(checkReachabilityWithIsExprNotConstantTrue3(), "java");
}

function testReachabilityWithIsExprNotConstantTrue4() {
    assertEqual(checkReachabilityWithIsExprNotConstantTrue4(), "python");
}

function testReachabilityWithIsExprNotConstantTrue5() {
    assertEqual(checkReachabilityWithIsExprNotConstantTrue5(), "other");
}

function testReachabilityWithIsExprNotConstantTrue6() {
    assertEqual(checkReachabilityWithIsExprNotConstantTrue6(), "C#");
}

function testReachabilityWithFailExpr() {
    assertEqual((<error>checkReachabilityWithFailExpr()).message(), "errormsg");
}

function testReachabilityWithFailExpr2() {
    assertEqual((<error>checkReachabilityWithFailExpr2()).message(), "errormsg");
}

function testReachabilityWithFailExpr3() {
    assertEqual((<error>checkReachabilityWithFailExpr3()).message(), "errormsg2");
}

function testReachabilityWithFailExpr4() {
    assertEqual(checkReachabilityWithFailExpr4(), "python");
}

function testReachabilityWithFailExpr5() {
    assertEqual((<error>checkReachabilityWithFailExpr5()).message(), "errormsg");
}

function testReachabilityWithFailExpr6() {
    assertEqual(checkReachabilityWithFailExpr6(), "C#");
}

function testReachabilityWithFailExpr7() {
    assertEqual((<error>checkReachabilityWithFailExpr7()).message(), "msg");
}

function testReachabilityWithResetTypeNarrowing() {
    assertEqual(checkReachabilityWithResetTypeNarrowing(), "car");
}

function testReachabilityWithResetTypeNarrowing2() {
    assertEqual(checkReachabilityWithResetTypeNarrowing2(), "car");
}

function testReachabilityWithResetTypeNarrowing3() {
    assertEqual(checkReachabilityWithResetTypeNarrowing3(), "jeep");
}

function testReachabilityWithResetTypeNarrowing4() {
    assertEqual(checkReachabilityWithResetTypeNarrowing4(), "jeep");
}

function testReachabilityWithResetTypeNarrowing5() {
    assertEqual(checkReachabilityWithResetTypeNarrowing5(), "van");
}

function testReachabilityWithResetTypeNarrowing6() {
    assertEqual(checkReachabilityWithResetTypeNarrowing6(), "van");
}

function testReachabilityWithResetTypeNarrowing7() {
    assertEqual(checkReachabilityWithResetTypeNarrowing7(), "jeep");
}

function testReachabilityWithResetTypeNarrowing8() {
    assertEqual(checkReachabilityWithResetTypeNarrowing8(), "jeep");
}

function testReachabilityWithResetTypeNarrowing9() {
    assertEqual(checkReachabilityWithResetTypeNarrowing9(), "jeep");
}

function testReachabilityWithResetTypeNarrowing10() {
    assertEqual(checkReachabilityWithResetTypeNarrowing10(), "van");
}

function testReachabilityWithResetTypeNarrowing11() {
    assertEqual(checkReachabilityWithResetTypeNarrowing11(), "jeep");
}

function testReachabilityWithResetTypeNarrowing12() {
    assertEqual(checkReachabilityWithResetTypeNarrowing12(), "jeep");
}

function testReachabilityWithResetTypeNarrowing13() {
    assertEqual(checkReachabilityWithResetTypeNarrowing13(), "car");
}

function testReachabilityWithResetTypeNarrowing14() {
    assertEqual(checkReachabilityWithResetTypeNarrowing14(), "car");
}

function testReachabilityWithResetTypeNarrowing15() {
    assertEqual(checkReachabilityWithResetTypeNarrowing15(), "car");
}

function testReachabilityWithResetTypeNarrowing16() {
    assertEqual(checkReachabilityWithResetTypeNarrowing16(), "car");
}

function testReachabilityWithIfStmtWithLogicalExprContainingEqualityExpr() {
    assertEqual(checkReachabilityWithIfStmtWithLogicalExprContainingEqualityExpr(), "bar");
}

function testReachabilityWithIfStmtWithAConditionUsingEnum() {
    assertEqual(checkReachabilityWithIfStmtWithAConditionUsingEnum(), "red");
}

function testReachabilityWithIfStmtWithAConditionUsingEnum2() {
    assertEqual(checkReachabilityWithIfStmtWithAConditionUsingEnum2(), "blue");
}

function testReachabilityWithIfStmtWithAConditionUsingEnum3() {
    assertEqual(checkReachabilityWithIfStmtWithAConditionUsingEnum3(), "blue");
}

function testReachabilityWithIfStmtWithAConditionUsingEnum4() {
    assertEqual(checkReachabilityWithIfStmtWithAConditionUsingEnum4(), "green");
}

function testReachabilityWithIfStmtWithAConditionUsingEnum5() {
    assertEqual(checkReachabilityWithIfStmtWithAConditionUsingEnum5(), "blue");
}

function testReachabilityWithWhile() {
    assertEqual(checkReachabilityWithWhile(), "pc");
}

function testReachabilityWithWhile2() {
    assertEqual(checkReachabilityWithWhile2(), "pc");
}

function testReachabilityWithWhile3() {
    assertEqual(checkReachabilityWithWhile3(), "pc");
}

function testReachabilityWithWhile4() {
    assertEqual(checkReachabilityWithWhile4(), 1);
}

function testReachabilityWithWhile5() {
    assertEqual(checkReachabilityWithWhile5(), 2);
}

function testReachabilityWithWhile6() {
    assertEqual(checkReachabilityWithWhile6(), 1);
}

function testReachabilityWithNestedWhile() {
    assertEqual(checkReachabilityWithNestedWhile(), "pc");
}

function testReachabilityWhereTheIsExprInsideWhile() {
    assertEqual(checkReachabilityWhereTheIsExprInsideWhile(), "ballerina");
}

function testReachabilityWhereTheIsExprInsideWhile2() {
    assertEqual(checkReachabilityWhereTheIsExprInsideWhile2(), "VSCode");
}

function testReachabilityWithWhileStmtWithEqualityExpr() {
    assertEqual(checkReachabilityWithWhileStmtWithEqualityExpr(), "lap");
}

function testReachabilityWithWhileStmtWithEqualityExpr2() {
    assertEqual(checkReachabilityWithWhileStmtWithEqualityExpr2(), "phone2");
}

function testReachabilityWithWhileStmtWithEqualityExpr3() {
    assertEqual(checkReachabilityWithWhileStmtWithEqualityExpr3(), "phone3");
}

function testReachabilityWithWhileStmtWithEqualityExpr4() {
    assertEqual(checkReachabilityWithWhileStmtWithEqualityExpr4(), 1);
}

function testReachabilityWithWhileStmtWithAConditionUsingEnum() {
    assertEqual(checkReachabilityWithWhileStmtWithAConditionUsingEnum(), "blue");
}

function testReachabilityWithWhileStmtWithAConditionUsingEnum2() {
    assertEqual(checkReachabilityWithWhileStmtWithAConditionUsingEnum2(), "blue");
}

function testReachabilityAfterCheck() {
    assertEqual(checkReachabilityAfterCheck(), "string");
}

function testReachabilityAfterCheckPanic() {
    assertEqual(checkReachabilityAfterCheckpanic(), "string");
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
