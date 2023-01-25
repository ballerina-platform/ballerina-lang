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

type MyError distinct error;

isolated function f1() returns int[]|error => [];

string globString = "1";

function testCheckInObjectFieldInitializer1() {
    MyError|object { function a; int b; int c; } a = f2();
    assertTrue(a is object { function a; int b; int c; });
    object { function a; int b; int c; } b = checkpanic a;
    function () returns error? c = <function () returns error?> b.a;
    assertTrue(c() is ());
    assertEquality(1, b.b);
    assertEquality(0, b.c);
    globString = "invalid";
    assertTrue(c() is error);

    MyError|object { function a; int b; int c; } d = f2();
    assertTrue(d is MyError);
    MyError e = <MyError> d;
    assertEquality("Error!", e.message());
    assertTrue(e.cause() is error);
    error f = <error> e.cause();
    assertEquality("{ballerina/lang.int}NumberParsingError", f.message());
    assertEquality("'string' value 'invalid' cannot be converted to 'int'", <string> checkpanic f.detail()["message"]);
    error:StackFrame[] callStack = f.stackTrace();
    int callStackLength = callStack.length();
    assertEquality("callableName: testCheckInObjectFieldInitializer1  " +
        "fileName: object_field_initializer_with_check.bal lineNumber: 34", callStack[2].toString());
    assertEquality("callableName: f2  fileName: object_field_initializer_with_check.bal lineNumber: 50", callStack[1].toString());
}

function f2() returns MyError|object { function a; int b; int c; } {
    var v = object {
            function a = function () returns error? {
                int q = check int:fromString(globString);
                record {|
                    any x;
                |} r = {x: check f1()};
            };
            int b = check int:fromString(globString);
            int c = 0;

            function init() returns error? {
            }
    };
    return v is error ? error MyError("Error!", v) : v;
}

isolated function f3() returns string|error|MyError => "";

function testCheckInObjectFieldInitializer2() {
    globString = "2";
    Bar|error a = new Bar();
    assertTrue(a is Bar);
    Bar b = checkpanic a;
    function () returns error? c = <function () returns error?> b.a;
    assertTrue(c() is error);
    assertEquality(2, b.b);
    assertEquality([], checkpanic b.c);
    assertEquality(["", ""], b.d);

    globString = "invalid";
    Bar|error d = new;
    assertTrue(d is error);
    error e = <error> d;
    assertEquality("{ballerina/lang.int}NumberParsingError", e.message());
    assertEquality("'string' value 'invalid' cannot be converted to 'int'", <string> checkpanic e.detail()["message"]);
    error:StackFrame[] callStack = e.stackTrace();
    int callStackLength = callStack.length();
    assertEquality("callableName: fromString moduleName: ballerina.lang.int.0 fileName: int.bal lineNumber: 173",
        callStack[callStackLength - 2].toString());
}

class Bar {
    function a = function () returns error? {
        int q = check int:fromString("invalid");
    };
    int b = check int:fromString(globString);
    int[]|error c = check f1();
    string[] d = ["", check f3()];

    function init() returns error? {
    }
}

function testCheckInObjectFieldInitializer3() {
    MyError|object { function a; int b; int c; } a = f4();
    assertTrue(a is object {});
    object { function a; int b; int c; } b = checkpanic a;
    assertEquality(0, b.b);

    f5Ind = 2;
    MyError|object { function a; int b; int c; } c = f4();
    assertTrue(c is MyError);
    error d = <error> c;
    assertEquality("f4!", d.message());

    f5Ind = "error";
    MyError|object { function a; int b; int c; } e = f4();
    assertTrue(e is MyError);
    error f = <error> e;
    assertEquality("f5", f.message());
}

function f4() returns MyError|object { function a; int b; int c; } {
    var v = object {
                function a = function () returns error? {
                    int q = check int:fromString("invalid");
                    object { function a; int b; int c; } r = check f2();
                };
                int b = check f5();
                int c = 0;

                function init() returns MyError? {
                    if self.b > 1 {
                        return error("f4!");
                    }
                }
    };
    return v;
}

0|1|2|"error" f5Ind = 0;

function f5() returns int|MyError {
    var f5IndLocal = f5Ind;
    if f5IndLocal is int {
        return f5IndLocal;
    } else {
        return error MyError("f5");
    }
}

type MyErrorTwo distinct error;

var ob = object {
    any|error a = check f1();
    object {
        function a;
        int b;
        int c;
    } b = check f2();
    string c = check f3();

    function init() returns error|MyError|MyErrorTwo? {
    }
};

final string|error notString = error MyError("error!");

var ob3 = object {
    any|error a = check f1();
    string b = check notString;

    function init() returns error? {
    }
};

function testCheckInObjectFieldInitializer4() {
    assertTrue(ob is object {});
    assertFalse(ob is error|MyError|MyErrorTwo);
    assertTrue(ob3 is MyError);
    assertFalse(ob3 is object {});
}

function testCheckInObjectFieldInitializer5() returns error? {
    globString = "invalid";
    var ob2 = object {
        any|error a = check f2();

        function init() returns MyError? {
        }
    };
    assertTrue(ob2 is MyError);
}

class Baz {
    function () returns int|error? a = () => check int:fromString("invalid");
    object {} b = check f2();

    function init() returns MyError? {
    }
}

function testCheckInObjectFieldInitializer6() {
    globString = "1";
    Baz|MyError a = new;
    assertTrue(a is Baz);

    globString = "invalid";
    Baz|MyError b = new;
    assertTrue(b is MyError);
}

isolated function assertTrue(anydata actual) => assertEquality(true, actual);

isolated function assertFalse(anydata actual) => assertEquality(false, actual);

isolated function assertEquality(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    panic error(string `expected '${expected.toString()}', found '${actual.toString()}'`);
}
