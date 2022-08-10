// Copyright (c) 2020 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

function constPattern1(string|int|boolean a) returns string {
    match a {
        12 => {
            return "12";
        }
        "Hello" => {
            return "Hello";
        }
        15 => {
            return "15";
        }
        true => {
            return "true";
        }
        false => {
            return "false";
        }
        "HelloAgain" => {
            return "HelloAgain";
        }
        _ => {
            return "Default";
        }
    }
}

function testConstPattern1() {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";
    string | int | boolean a3 = true;
    string | int | boolean a4 = 15;
    string | int | boolean a5 = "HelloAgain";
    string | int | boolean a6 = false;
    string | int | boolean a7 = "NothingToMatch";

    assertEquals("12", constPattern1(a1));
    assertEquals("Hello", constPattern1(a2));
    assertEquals("true", constPattern1(a3));
    assertEquals("15", constPattern1(a4));
    assertEquals("HelloAgain", constPattern1(a5));
    assertEquals("false", constPattern1(a6));
    assertEquals("Default", constPattern1(a7));
}

function constPattern2(string|int|boolean a, string|int|boolean b) returns string {
    match a {
        12 => {
            return "Value is '12'";
        }
        "Hello" => {
            return "Value is 'Hello'";
        }
        15 => {
            match b {
                34 => {
                    return "Value is '15 & 34'";
                }
                "HelloWorld" => {
                    return "Value is '15 & HelloWorld'";
                }
            }
        }
        "HelloAgain" => {
            match b {
                34 => {
                    return "Value is 'HelloAgain & 34'";
                }
                "HelloWorld" => {
                    return "Value is 'HelloAgain & HelloWorld'";
                }
            }
        }
        true => {
            return "Value is 'true'";
        }
    }

    return "Value is 'Default'";
}

function testConstPattern2() {
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";

    string | int | boolean a3 = 15;
    string | int | boolean a4 = "HelloWorld";

    string | int | boolean a5 = "HelloAgain";
    string | int | boolean a6 = 34;

    string | int | boolean a7 = "NothingToMatch";
    string | int | boolean a8 = false;

    string | int | boolean a9 = 15;
    string | int | boolean a10 = 34;

    string | int | boolean a11 = true;
    string | int | boolean a12 = false;

    assertEquals("Value is '12'", constPattern2(a1, a2));
    assertEquals("Value is '15 & HelloWorld'", constPattern2(a3, a4));
    assertEquals("Value is 'HelloAgain & 34'", constPattern2(a5, a6));
    assertEquals("Value is 'Default'", constPattern2(a7, a8));
    assertEquals("Value is '15 & 34'", constPattern2(a9, a10));
    assertEquals("Value is 'true'", constPattern2(a11, a12));
}

function constPattern3(string|int|boolean a) returns string {
    match a {
        12 | 13 | 14 => {
            return "Value is : " + a.toString();
        }
        "Hello" | "World" => {
            return "Value is : " + a.toString();
        }
        15 | "Test" => {
            return "Value is : " + a.toString();
        }
        true | false => {
            return "Value is : " + a.toString();
        }
        "HelloAgain" => {
            return "Value is : " + a.toString();
        }
    }

    return "Default value is : " + a.toString();
}

function testConstPattern3(){
    string | int | boolean a1 = 12;
    string | int | boolean a2 = "Hello";
    string | int | boolean a3 = true;

    string | int | boolean a4 = 15;
    string | int | boolean a5 = "HelloAgain";
    string | int | boolean a6 = false;

    string | int | boolean a7 = "NothingToMatch";
    string | int | boolean a8 = 13;
    string | int | boolean a9 = 14;
    string | int | boolean a10 = "World";
    string | int | boolean a11 = "Test";

    assertEquals("Value is : 12", constPattern3(a1));
    assertEquals("Value is : Hello", constPattern3(a2));
    assertEquals("Value is : true", constPattern3(a3));
    assertEquals("Value is : 15", constPattern3(a4));
    assertEquals("Value is : HelloAgain", constPattern3(a5));
    assertEquals("Value is : false", constPattern3(a6));
    assertEquals("Default value is : NothingToMatch", constPattern3(a7));
    assertEquals("Value is : 13", constPattern3(a8));
    assertEquals("Value is : 14", constPattern3(a9));
    assertEquals("Value is : World", constPattern3(a10));
    assertEquals("Value is : Test", constPattern3(a11));
}

function testConstPattern4() {
    string result = "";
    any|error a = 12;
    match a {
        1 | 2 => {result = "1|2";}
        3 | 4 => {result = "3|4";}
        11 | 12 => {result = "11|12";}
        _ => {result = "Default";}
    }

    assertEquals("11|12", result);
}

const CONST1 = "Ballerina";
const CONST2 = 200;
const NIL = ();

function constPattern5(any a) returns string {
    match a {
        100 => {
            return "100";
        }
        CONST2 => {
            return "200";
        }
        "Bal" => {
            return "Bal";
        }
        NIL => {
            return "Nil";
        }
        CONST1 => {
            return "Ballerina";
        }
        _ => {
            return "Default";
        }
    }
}

function testConstPattern5() {
    string a1 = "Bal";
    string a2 = "Ballerina";
    int a3 = 100;
    int a4 = 200;
    () a5 = ();
    float a6 = 100.0;

    assertEquals("Bal", constPattern5(a1));
    assertEquals("Ballerina", constPattern5(a2));
    assertEquals("100", constPattern5(a3));
    assertEquals("200", constPattern5(a4));
    assertEquals("Nil", constPattern5(a5));
    assertEquals("Default", constPattern5(a6));
}

const CONST_1 = "A";
const CONST_2 = "B";
const CONST_3 = 10;
const CONST_4 = true;

function constPattern6(CONST_1|CONST_2|CONST_3|CONST_4 a) returns string {
    string results = "";

    match a {
        CONST_2 => {
            results += "B";
        }
        CONST_1 => {
            results += "A";
        }
        true => {
            results += "true";
        }
        CONST_3 => {
            results += "10";
        }
    }

    match a {
        10 => {
            results += "10";
        }
        "A" => {
            results += "A";
        }
        CONST_4 => {
            results += "true";
        }
        "B" => {
            results += "B";
        }
    }

    return results;
}

function testConstPattern6() {
    assertEquals("AA", constPattern6(CONST_1));
    assertEquals("BB", constPattern6(CONST_2));
    assertEquals("1010", constPattern6(CONST_3));
    assertEquals("truetrue", constPattern6(CONST_4));
}

function testConstPattern7() {
    any|error v = error("{UserGenError}Error");
    string result = "no-match";

    match v {
        0 => { result = "zero"; }
        _ => { result = "other"; }
    }

    assertEquals("no-match", result);
}

function testConstPattern8() {
    json j = null;
    string result = "";

    match j {
        0 => {result = "0";}
        null => {result = "null";}
    }

    assertEquals("null", result);
}

function constPattern9(int v) returns string {
    string s;

    match v {
        1 => {
            s = "ONE";
        }
        2 => {
            s = "TWO";
        }
        3 => {
            s = "THREE";
        }
        _ => {
            s = "OTHER";
        }
    }
    return s;
}

function testConstPattern9() {
    assertEquals("ONE", constPattern9(1));
    assertEquals("TWO", constPattern9(2));
    assertEquals("THREE", constPattern9(3));
    assertEquals("OTHER", constPattern9(4));
}

function constPattern10(int v) returns string {
    string s;

    match v {
        1 => {
            s = "ONE";
        }
        _ => {
            match v {
                2 => {
                    s = "TWO";
                }
                _ => {
                    s = "OTHER";
                }
            }
        }
    }
    return s;
}

function testConstPattern10() {
    assertEquals("ONE", constPattern10(1));
    assertEquals("TWO", constPattern10(2));
    assertEquals("OTHER", constPattern10(3));
}

function constPattern11(int v1, int v2) returns string {
    string s;

    match v1 {
        1 => {
            s = "ONE";
        }
        2 => {
            s = "TWO";
        }
        _ => {
            match v2 {
                2 => {
                    s = "TWO";
                }
                _ => {
                    s = "OTHER";
                }
            }
        }
    }
    return s;
}

function testConstPattern11() {
    assertEquals("ONE", constPattern11(1, 1));
    assertEquals("TWO", constPattern11(2, 5));
    assertEquals("TWO", constPattern11(3, 2));
    assertEquals("OTHER", constPattern11(3, 3));
}

function constPattern12(int v) returns string {
    string s = "";

    match v {
        1 => {
            s = "ONE";
        }
        2 => {
            s = "TWO";
        }
        _ => {
            return "OTHER";
        }
    }
    return s;
}

function testConstPattern12() {
    assertEquals("ONE", constPattern12(1));
    assertEquals("TWO", constPattern12(2));
    assertEquals("OTHER", constPattern12(3));
}

client class Client {
    remote function post(string x, string y) returns anydata {
        return y;
    }
}

Client clientEndpoint = new ();
public function testConstPattern13() {
    string animal = "Mouse";
    match animal {
        "Mouse" => {
            var res = clientEndpoint->post("/post", "POST: Hello World");
            assertEquals("POST: Hello World", res);
        }
        _ => {
            panic error("No match found");
        }
    }
}

function constPattern14(any a, any b) returns string {
    match a {
        "foo" => {
            match b {
                "bar" => {
                    return "s1";
                }
                _ => {
                    return "s2";
                }
            }
        }
        _ => {
            match b {
                "bar" => {
                    return "s3";
                }
                _ => {
                    return "s4";
                }
            }
        }
    }
}

function testConstPattern14() {
    assertEquals("s2", constPattern14("foo", 2));
    assertEquals("s1", constPattern14("foo", "bar"));
    assertEquals("s4", constPattern14(1, 2));
    assertEquals("s3", constPattern14(1, "bar"));
}

public function constPattern15(any animal) returns string {
    int total = 1;
    match animal {
        "Mouse" => {
            if (total == 2) {
                return "Total 2";
            } else {
                return "Not 2";
            }
        }
        _ => {
            return "!Mouse";
        }
    }
}

public function testConstPattern15() {
    assertEquals("Not 2", constPattern15("Mouse"));
    assertEquals("!Mouse", constPattern15("Dog"));
}

public function constPattern16(any animal) returns string {
    int total = 1;
    int age = 2;
    match animal {
        "Mouse" => {
            if (total == 2) {
                if (age > 3) {
                    return "Age is greater that 3";
                } else {
                    return "Age is less than 3";
                }
            } else {
                if (age > 3) {
                    return "Age is greater that 3";
                } else {
                    return "Age is less than 3";
                }
            }
        }
        _ => {
            return "!Mouse";
        }
    }
}

public function testConstPattern16() {
    assertEquals("Age is less than 3", constPattern16("Mouse"));
    assertEquals("!Mouse", constPattern16("Dog"));
}

function constPattern17(any x) returns int {
    match x {
        () => {
            return 1;
        }
        true => {
            return 2;
        }
        -1 => {
            return 3;
        }
        _ => {
            return 4;
        }
    }
}

public function testConstPattern17() {
    assertEquals(1, constPattern17(()));
    assertEquals(2, constPattern17(true));
    assertEquals(3, constPattern17(-1));
    assertEquals(4, constPattern17(5));
}

function testConstPattern18() {
    string result = "";
    any|error a = 12;
    match a {
        1 | 2 => {
            result = "1|2";
        }
        3 | 4 => {
            result = "3|4";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Default", result);

    string|error b = "John";
    match b {
        "Kate" | "Anne" => {
            result = "1|2";
        }
        "James" => {
            result = "3|4";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Default", result);

    any|error c = error("SimpleError");
    result = "Not matched";
    match c {
        "Kate" | "Anne" => {
            result = "1|2";
        }
        "James" => {
            result = "3|4";
        }
        _ => {
            result = "Default";
        }
    }
    assertEquals("Not matched", result);

    string|boolean|int|error d = "Ballerina";
    result = "";
    match d {
        12 => {
            result = "int value 1";
        }
        "Hello" => {
            result = "string value 1";
        }
        _ => {
            match d {
                34 => {
                    result = "int value 2";
                }
                _ => {
                    result = "Default";
                }
            }
        }
    }
    assertEquals("Default", result);
}

type A int[2] & readonly;

function constPattern19(A x) returns int {
    match x {
        [1, 2] => {
            return 1;
        }
        [-2, -7] => {
            return 2;
        }
        _ => {
            return 4;
        }
    }
}

public function testConstPattern19() {
    assertEquals(1, constPattern19([1, 2]));
    assertEquals(2, constPattern19([-2, -7]));
    assertEquals(4, constPattern19([2, 4]));
}

enum FOO {
    X,
    Y
}

function constPattern20(FOO 'type, int|string g) returns string {
    match 'type {
        X if g is string => {
            return "A";
        }
        Y if g is string => {
            return "B";
        }
        _ => {
            return string `no match ${'type.toString()}`;
       }
    }
}

function testConstPattern20() {
    assertEquals(constPattern20(X, "AAA"), "A");
    assertEquals(constPattern20(Y, "BBB"), "B");
    assertEquals(constPattern20(X, 1), "no match X");
    assertEquals(constPattern20(Y, 2), "no match Y");
}

function constPatternWithNegativeLiteral(any x) returns string {
    match x {
        -1 => {
            return "-1";
        }
        -12.3 => {
            return "-12.3";
        }
        1 => {
            return "1";
        }
        _ => {
            return "other";
        }
    }
}

function testConstPatternWithNegativeLiteral() {
    assertEquals("-1", constPatternWithNegativeLiteral(-1));
    assertEquals("-12.3", constPatternWithNegativeLiteral(-12.3f));
    assertEquals("1", constPatternWithNegativeLiteral(1));
    assertEquals("other", constPatternWithNegativeLiteral(0));
    assertEquals("other", constPatternWithNegativeLiteral(-123.4));
}

function testConstPatternWithPredeclaredPrefix() {
    assertEquals(0, constPatternWithPredeclaredPrefix1(int:MAX_VALUE));
    assertEquals(1, constPatternWithPredeclaredPrefix1(int:MIN_VALUE));
    assertEquals(2, constPatternWithPredeclaredPrefix1(2));

    assertEquals(0, constPatternWithPredeclaredPrefix2([int:MIN_VALUE, int:MAX_VALUE]));
    assertEquals(1, constPatternWithPredeclaredPrefix2([int:MAX_VALUE, int:MIN_VALUE]));

    assertEquals(0, constPatternWithPredeclaredPrefix3({i: int:MIN_VALUE, j: int:MAX_VALUE}));
    assertEquals(0, constPatternWithPredeclaredPrefix3({i: int:MIN_VALUE, j: int:MAX_VALUE, k: 2}));
    assertEquals(1, constPatternWithPredeclaredPrefix3({i: int:MIN_VALUE}));
    assertEquals(1, constPatternWithPredeclaredPrefix3({i: 2, j: 3}));
}

function constPatternWithPredeclaredPrefix1(int x) returns int {
    match x {
        int:MAX_VALUE => {
            return 0;
        }
        int:MIN_VALUE => {
            return 1;
        }
        _ => {
            return 2;
        }
    }
}

function constPatternWithPredeclaredPrefix2(int[] x) returns int {
    match x {
        [int:MIN_VALUE, int:MAX_VALUE] => {
            return 0;
        }
        _ => {
            return 1;
        }
    }
}

function testMatchStmtInsideForeachString1() {
    string input = "1x2";
    string output = "";
    foreach string char in input {
        match char {
            "1" => {
                output = output.concat("One");
            }
            "2" => {
                output = output.concat("Two");
            }
            _ => {
                output = output.concat("Other");
            }
        }
    }
    assertEquals("OneOtherTwo", output);
}

type CHAR string;

function testMatchStmtInsideForeachString2() {
    CHAR input = "1x2";
    string output = "";
    foreach string char in input {
        match char {
            "1" => {
                output = output.concat("One");
            }
            "2" => {
                output = output.concat("Two");
            }
            _ => {
                output = output.concat("Other");
            }
        }
    }
    assertEquals("OneOtherTwo", output);
}

function testMatchStmtInsideForeachInt() {
    int:Signed8[] input = [7, 2, 2];
    string output = "";
    foreach int number in input {
        match number {
            2 => {
                output = output.concat("Two");
            }
            _ => {
                output = output.concat("Other");
            }
        }
    }
    assertEquals("OtherTwoTwo", output);
}

function testMatchStmtInsideForeach() {
    testMatchStmtInsideForeachString1();
    testMatchStmtInsideForeachString2();
    testMatchStmtInsideForeachInt();
}

function constPatternWithPredeclaredPrefix3(map<int> x) returns int {
    match x {
        {i: int:MIN_VALUE, j: int:MAX_VALUE} => {
            return 0;
        }
        _ => {
            return 1;
        }
    }
}

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error("expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
