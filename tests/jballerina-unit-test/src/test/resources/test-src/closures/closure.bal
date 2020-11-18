// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

import ballerina/lang.'string as strings;

int globalA = 5;

function basicTest() returns (function (int) returns (int)) {
    int methodInt = 8;
    int anotherMethodInt = 3;
    var addFunc = function (int funcInt) returns (int) {
        return funcInt + methodInt + anotherMethodInt;
    };
    return addFunc;
}

function test1() returns int {
    var foo = basicTest();
    return foo(7);
}

function twoLevelTest() returns (function (int) returns (int)) {
    int methodInt1 = 2;
    var addFunc1 = function (int funcInt1) returns (int) {
        int methodInt2 = 23;
        var addFunc2 = function (int funcInt2) returns (int) {
            return funcInt2 + methodInt1 + methodInt2;
        };
        return addFunc2(5) + funcInt1;
    };
    return addFunc1;
}

function test2() returns int {
    var foo = twoLevelTest();
    return foo(6);
}

function threeLevelTest() returns (function (int) returns (int)) {
    int methodInt1 = 2;
    var addFunc1 = function (int funcInt1) returns (int) {
        int methodInt2 = 23;
        var addFunc2 = function (int funcInt2) returns (int) {
            int methodInt3 = 7;
            var addFunc3 = function (int funcInt3) returns (int) {
                return funcInt3 + methodInt1 + methodInt2 + methodInt3;
            };
            return addFunc3(8) + funcInt2;
        };
        return addFunc2(4) + funcInt1;
    };
    return addFunc1;
}

function test3() returns int {
    var foo = threeLevelTest();
    return foo(6);
}

function closureWithIfBlock() returns (function (int) returns (int)) {
    int a = 3;
    var addFunc =  function (int b) returns (int) {
        int c = 34;
        if (b == 3) {
            int e = 34;
            c = b + e;
        }
        return b + c + a;
    };
    return addFunc;
}

function test4() returns int {
    var foo = closureWithIfBlock();
    return foo(3);
}


function getFunc1(int functionIntX) returns (function (int) returns (function (int) returns (int))) {
    return function (int functionIntY) returns (function (int) returns (int)) {
        return function (int functionIntZ) returns (int) {
            return functionIntX + functionIntY + functionIntZ;
        };
    };
}

function test5() returns (int){
    var getFunc2 = getFunc1(1);
    var getFunc3 = getFunc2(5);
    return getFunc3(4);
}

function getIncFunc(int x) returns (function (int) returns (int)) {
    return function (int y) returns (int) {
        return x + y;
    };
}

function test6() returns (int){
    var incBy1 = getIncFunc(1);
    var incBy5 = getIncFunc(5);
    var incBy10 = getIncFunc(10);

    return (incBy1(100) + incBy5(100) + incBy10(100));
}


function out2ndFunc(int out2ndParam) returns (function (int) returns int) {
    int e = 45;
    int out2ndFuncLocal = 8;
    int f = 45;
    var out1stFunc = function (int out1stParam) returns (int) {
        var inner1Func = function (int inner1Param) returns (int) {
                int g = out2ndFuncLocal + out2ndParam;
                int h = out1stParam + inner1Param;
                return g + h;
            };
        return  inner1Func(3);
    };
    return out1stFunc;
}

function test7() returns int {
    var foo = out2ndFunc(4);
    return foo(7);
}

function testMultiLevelFunction() returns (int) {
    var addFunc1 = function (int funcInt1) returns (int) {
        var addFunc2 = function (int funcInt2) returns (int) {
            var addFunc3 =  function (int methodInt3, int methodInt2, int methodInt1, int funcInt3) returns (int) {
                return funcInt3 + methodInt1 + methodInt2 + methodInt3;
            };
            return addFunc3(7, 23, 2, 8) + funcInt2;
        };
        return addFunc2(4) + funcInt1;
    };
    return addFunc1(6);
}

function test8() returns int {
    return testMultiLevelFunction();
}

function globalVarAccessTest() returns (function (int) returns (int)) {
    int methodInt = 8;
    int anotherMethodInt = 3;
    var addFunc = function (int funcInt) returns (int) {
        return funcInt + methodInt + globalA + anotherMethodInt;
    };
    return addFunc;
}

function test9() returns int {
    var foo = globalVarAccessTest();
    return foo(7);
}

function testDifferentTypeArgs1() returns (function (int, float) returns (int)) {
    int methodInt = 8;
    int anotherMethodInt = 3;
    var addFunc = function (int funcInt, float funcFloat) returns (int) {
        int f2i = <int>funcFloat;
        return funcInt + methodInt + anotherMethodInt + f2i;
    };
    return addFunc;
}

function test10() returns int {
    var foo = testDifferentTypeArgs1();
    return foo(7, 2.3);
}

function testDifferentTypeArgs2(int functionIntX) returns (function (float) returns (function (boolean) returns (int))) {
    return function (float functionFloatY) returns (function (boolean) returns (int)) {
        return function (boolean functionBoolZ) returns (int) {
            if (functionBoolZ) {
                return functionIntX + <int>functionFloatY;
            }
            return functionIntX;
        };
    };
}

function test11() returns int {
    var f1 = testDifferentTypeArgs2(7);
    var f2 = f1(2.6);
    return f2(true);
}


function testDifferentTypeArgs3(int a1, float a2) returns (function (boolean, float) returns (function () returns (int))) {
    return function (boolean b1, float b2) returns (function () returns (int)) {
        var foo = function () returns (int) {
            if (b1) {
                return a1 + <int>a2;
            }
            return a1 + <int>b2;
        };
        return foo;
    };
}

function test12() returns int {
    var f1 = testDifferentTypeArgs3(7, 3.2);
    var f2 = f1(true, 2.3);
    return f2();
}

function test13() returns int {
    var f1 = testDifferentTypeArgs3(7, 3.2);
    var f2 = f1(false, 2.3);
    return f2();
}

function getStringFunc1(string functionX) returns (function (string) returns (function (string) returns (string))) {
    return function (string functionY) returns (function (string) returns (string)) {
        return function (string functionZ) returns (string) {
            return functionX + functionY + functionZ;
        };
    };
}

function test14() returns (string){
    var getStringFunc2 = getStringFunc1("Hello");
    var getStringFunc3 = getStringFunc2("Ballerina");
    return getStringFunc3("World!!!");
}

function testWithVarArgs() returns (function (int) returns (int)) {
    int methodInt = 8;
    int anotherMethodInt = 3;
    var addFunc = function (int funcInt) returns (int) {
        return funcInt + methodInt + anotherMethodInt;
    };
    return addFunc;
}

function test15() returns int {
    var foo = basicTest();
    return foo(7);
}

function testClosureWithTupleTypes([string, float, string] g) returns (function (string, [string, float, string]) returns (string)){
    return function (string x, [string, float, string] y) returns (string) {
       var [i, j, k] = y;
       var [l, m, n] = g;
       return x + i + j.toString() + k + l + m.toString() + n.toString();
    };
}

function test16() returns string {
    string a = "Hello";
    float b = 11.1;
    string c = "World !!!";
    var foo = testClosureWithTupleTypes([a, b, c]);
    string i = "Ballerina";
    float j = 15.0;
    string k = "Program !!!";
    return foo("Im", [i, j, k]);
}

function testClosureWithTupleTypesOrder([string, float, string] g) returns (function ([string, float, string], string) returns (string)){
    string i = "HelloInner";
    float j = 44.8;
    string k = "World Inner!!!";
    [string, float, string] r = [i, j, k];

    return function ([string, float, string] y, string x) returns (string) {
       var [a, b, c] = g;
       var [d, e, f] = y;
       var [i1, j1, k1] = r;

       return x + a + b.toString() + c + d + e.toString() + f + i1 + j1.toString() + k1;
    };
}

function test17() returns string {
    string d = "Ballerina";
    float e = 15.0;
    string f = "Program!!!";

    string a = "Hello";
    float b = 11.1;
    string c = "World !!!";

    var foo = testClosureWithTupleTypesOrder([a, b, c]);
    return foo([d, e, f], "I'm");
}

function globalVarAccessAndModifyTest() returns (int) {
    int a = 3;
    a = 6;
    globalA = 7;
    var addFunc = function (int b) returns (int) {
        return b + globalA + a;
    };
    return addFunc(3);
}

function test18() returns int {
    return globalVarAccessAndModifyTest();
}

class Person {
    public int age = 3;
    public string name = "Hello Ballerina";

    private int year = 5;
    private string month = "february";

    function getAttachedFn() returns string {
        int b = 4;
        var foo = function (float w) returns (string) {
           return self.name + w.toString() + "K" + b.toString() + self.age.toString();
        };
        return foo(7.4);
    }

    function getAttachedFP() returns function (float) returns (string) {
        int b = 4;
        var foo = function (float w) returns (string) {
            return w.toString() + self.year.toString() + b.toString() + "Ballerina !!!";
        };
        return foo;
    }
}

function test19() returns (string) {
    Person p = new;
    return p.getAttachedFn();
}

function test20() returns (string) {
    Person p = new;
    var foo = p.getAttachedFP();
    return foo(7.3);
}

function testDifferentArgs() returns (function (float) returns (function (float) returns (string))) {
    int outerInt = 4;
    boolean booOuter = false;
    var outerFoo = function (float fOut) returns (function (float) returns (string)) {
        int innerInt = 7;
        boolean booInner = true;
        var innerFoo = function (float fIn) returns (string) {
            string str = "Plain";
            if (!booOuter && booInner) {
                str = innerInt.toString() + "InnerInt" + outerInt.toString() + fOut.toString() + "InnerFloat" +
                        fIn.toString() + "Ballerina !!!";
            }
            return str;
        };
        return innerFoo;
    };
    return outerFoo;
}

function test22() returns (string) {
    var fooOut = testDifferentArgs();
    var fooIn = fooOut(1.2);
    return fooIn(4.5);
}

function testVariableShadowingInClosure1(int a) returns function (float) returns (string){
    int b = 4;
    float f = 5.6;

    if (a < 10) {
        b = a + b + <int>f;
    }

    var foo = function (float f) returns (string) {
        if (a > 8) {
            b = a + <int>f + b;
        }
        return "Ballerina" + b.toString();
    };
    return foo;
}


function test23() returns string {
    var foo = testVariableShadowingInClosure1(9);
    string a = foo(3.4);
    return a;
}

function testVariableShadowingInClosure2(int a) returns function (float) returns (function (float, boolean) returns (string)){
    int b = 4;
    float f = 5.6;
    boolean boo = true;

    if (a < 10) {
        b = a + b + <int>f;
    }

    var fooOut = function (float f) returns (function (float, boolean) returns (string)) {
        if (a > 8) {
            b = a + <int>f + b;
        }
        string s = "Out" + b.toString();

        var fooIn = function (float f, boolean boo) returns (string) {
            if (a > 8 && !boo) {
                b = a + <int>f + b;
            }
            return s + "In" + b.toString() + "Ballerina!!!";
        };
        return fooIn;
    };
    return fooOut;
}


function test24() returns string {
    var foo = testVariableShadowingInClosure2(9);
    var bar = foo(3.4);
    string s = bar(24.6, false);
    return s;
}

function testVariableShadowingInClosure3(int a) returns (function (float) returns (function (float) returns
                                                                        (function (float, boolean) returns (string)))){
    int b = 4;
    float f = 5.6;
    boolean boo = true;

    if (a < 10) {
        b = a + b + <int>f;
    }

    var fooOutMost = function (float f) returns (function (float) returns (function (float, boolean) returns (string))) {
        if (a > 8) {
            b = a + <int>f + b;
        }
        string sOut = "OutMost" + b.toString();

        var fooOut = function (float f) returns (function (float, boolean) returns (string)) {
            if (a == 9) {
                b = a + <int>f + b;
            }
            string s = sOut + "Out" + b.toString();

            var fooIn = function (float f, boolean boo) returns (string) {
                if (a > 8 && !boo) {
                    b = a + <int>f + b;
                }
                return s + "In" + b.toString() + "Ballerina!!!";
            };
            return fooIn;
        };
        return fooOut;
    };
    return fooOutMost;
}

function test25() returns string {
    var foo = testVariableShadowingInClosure3(9);
    var bar = foo(3.4);
    var baz = bar(5.7);
    string s = baz(24.6, false);
    return s;
}


function testVariableShadowingInClosure4() returns (function (float) returns (function (float) returns
                                                                        (function (float, boolean) returns (string)))){
    int b = 4;
    int a = 7;
    float f = 5.6;
    boolean boo = true;

    var fooOutMost = function (float f) returns (function (float) returns (function (float, boolean) returns (string))) {
        string sOut = "OutMost" + b.toString() + a.toString();

        var fooOut = function (float f) returns (function (float, boolean) returns (string)) {
            string s = sOut + "Out" + b.toString() + a.toString();

            var fooIn = function (float f, boolean boo) returns (string) {
                b = a + <int>f + b;
                return s + "In" + b.toString() + "Ballerina!!!";
            };
            return fooIn;
        };
        return fooOut;
    };
    return fooOutMost;
}

function test26() returns string {
    var foo = testVariableShadowingInClosure4();
    var bar = foo(3.4);
    var baz = bar(5.7);
    string s = baz(24.6, false);
    return s;
}

function testLocalVarModifyWithinClosureScope() returns (float){
    float fadd = 0.0;
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    fa.forEach(function (float i) { fadd = fadd + i;});
    float fsum = fadd;
    return (fsum);
}

function testByteAndBoolean() returns (function (int, byte) returns
                    ((function (byte, int, boolean) returns byte[][]))) {
    boolean boo1 = true;
    return function (int a, byte b) returns (function (byte, int, boolean) returns byte[][]) {
        boolean boo2 = false;
        return function (byte c, int f, boolean booF) returns (byte[][]) {
            var value = trap <byte> f;
            if (value is byte) {
                byte i = value;
                byte[][] bArr = [];
                if !boo2 {
                    bArr[0] = [c, b, 4, i];
                }

                if boo1 {
                    bArr[1] = [i, c, 5, b, 3];
                }

                if !booF {
                    bArr[2] = [1, 2, 3, c, b];
                }

                return bArr;
            } else {
                panic value;
            }
        };
    };
}

function test27() returns byte[][] {
    var foo = testByteAndBoolean();
    var bar = foo(34, 7);
    return bar(13, 3, false);
}

function testMultiLevelBlockStatements1() returns (function () returns (function(int) returns int)) {
    int sum1 = 23;
    var bar = function () returns (function (int) returns int) {
        float f = 23.7;
        var foo = function (int i) returns (int) {
            int sum2 = 7;
            if (i < 7) {
                if (i < 6) {
                    if (i < 5) {
                        if (i == 4) {
                            sum1 = sum1 + sum2 + i + <int>f;
                        }
                    }
                }
            }
            return sum1;
        };
        return foo;
    };
    return bar;
}

function testMultiLevelBlockStatements2() returns (function(int[], int[], int[]) returns int) {
    int sum = 23;
    var foo = function (int[] i, int[] j, int[] k) returns int {
        foreach var x in i {
            foreach var y in j {
                foreach var z in k {
                    sum = sum + x + y + z;
                }
            }
        }
        return sum;
    };

    return foo;
}


function test28() returns [int, int] {
    var foo = testMultiLevelBlockStatements1();
    var baz = foo();
    var bar = testMultiLevelBlockStatements2();
    int[] i = [1,2];
    int[] j = [1,2,3];
    int[] k = [1,2,3,4];
    return [baz(4), bar(i,j,k)];
}


function function1(any firstParameter) returns (function (any) returns boolean) {
    return function (any secondParameter) returns boolean {
        return firstParameter === secondParameter;
    };
}

function function2(string firstParameter) returns (function (string) returns boolean) {
    return function (string secondParameter) returns boolean {
        return firstParameter == secondParameter;
    };
}

function function3(any firstParameter) returns (function (any) returns boolean) {
    var otherInternal = firstParameter;
    return function  (any secondParameter) returns boolean {
        return otherInternal === secondParameter;
    };
}

function function4(string firstParameter) returns (function (string) returns boolean) {
    var otherInternal = firstParameter;
    return function (string secondParameter) returns boolean {
        return otherInternal == secondParameter;
    };
}

function test29() returns [boolean, boolean, boolean, boolean, boolean, boolean, boolean, boolean] {
    var a1 = function1("ABC");
    boolean b10 = a1("ABC");
    boolean b11 = a1("GHJ");

    var a2 = function2("ABC");
    boolean b20 = a2("ABC");
    boolean b21 = a2("WER");

    var a3 = function3("ABC");
    boolean b30 = a3("ABC");
    boolean b31 = a3("MKJ");

    var a4 = function4("ABC");
    boolean b40 = a4("ABC");
    boolean b41 = a4("ERWWS");

    return [b10, b11, b20, b21, b30, b31, b40, b41];
}

function test30() returns int|string {
    int|string x = 5;
    var addFunc1 = function () {
        if (x is int && x > 3) {
            x = x + 1;
        } else {
            x = 0;
        }
    };
    var y = addFunc1();
    return x;
}

function test31() returns int|string {
    int|string x = 5;
    var func1 = function () {
        if (x is int && x > 3) {
            x = x + 1;
            if (x > 5) {
               x = x + 10;
            }
        } else {
            x = 0;
        }
         var func2 = function () {
            if (x is int && x > 5) {
                x = x + 1;
                var func3 = function () {
                    if (x is int && x > 10) {
                        x = x + 1;
                    } else {
                        x = 0;
                    }
                 };
                 _ = func3();
            } else {
                x = 0;
            }
         };
         _ = func2();
    };
    _ = func1();

    if (x is int && x > 3) {
        x = x + 10;
        if (x > 25) {
            x = x + 3;
        }
    } else {
        x = 0;
    }
    return x;
}

public function laterInitCapture() returns string {
    string a;
    boolean test = true;
    if test {
        a = "a";
    } else {
        a = "b";
    }

    var bar = function () returns string {
        a = a + "a";
        return a;
    };

    return bar();
}

function testRestParamsAsClosureVars() returns string {
    return funcAcceptingRestArgs("Hello", "From", "Ballerina");
}

function funcAcceptingRestArgs(string... args) returns string {
    var fn = function () returns string {
        string result = strings:'join(", ", ...args);
        return result;
    };
    return fn();
}

function testRestParamsAsClosureVars2() returns int {
    return funcAcceptingIntArgs(10, 20, 30);
}

function funcAcceptingIntArgs(int... scores) returns int {
    var fn = function () returns int {
        int len = scores.length();
        int i = 0;
        int sum = 0;

        while (i < len) {
            sum += scores[i];
            i += 1;
        }

        return sum;
    };
    return fn();
}
