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

function testReachableCodeWithIf1() {
    if false {
        error:unreachable();
        error:unreachable();
    }
    int|string c = 25;
}

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
    } else {
        error:unreachable();
        error:unreachable();
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
    } else {
        error:unreachable();
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
    error:unreachable();
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
    error:unreachable();
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
        error:unreachable();
    }
    if i is boolean {
        return "boolean";
    }
    error:unreachable();
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
                } else {
                    error:unreachable();
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
        } else {
            error:unreachable();
        }
        string s = " -> after true block2";
        str += s;
    } else {
        xml y = x;
        str += "xml";
    }
    return str;
}

function testReachableCodeWithWhile1() {
    while false {
        error:unreachable();
        error:unreachable();
    }
    int|string c = 25;
}

function testReachableCodeWithWhile2() {
    while true {
        string a = "A";
        return;
        error:unreachable();
    }
    error:unreachable();
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
    error:unreachable();
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
                error:unreachable();
            }
            if b is int {
                break;
            }
            error:unreachable();
        } else {
            return 2;
        }
        return 3;
    }
    return 4;
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
                    str += a;
                    break;
                } else if v is () {
                    i += 1;
                    panic getError();
                }
                error:unreachable();
            }
            if a is int {
                if true {
                    str += "DEF";
                    fail getError();
                }
                error:unreachable();
            }
            error:unreachable();
        }
    }
}

function getError() returns error {
    error err = error("Custom Error");
    return err;
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
