int globalA = 5;

function basicTest() returns (function (int) returns (int)) {
    int methodInt = 8;
    int anotherMethodInt = 3;
    var addFunc = (int funcInt) => (int) {
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
    var addFunc1 = (int funcInt1) => (int) {
        int methodInt2 = 23;
        var addFunc2 = (int funcInt2) => (int) {
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
    var addFunc1 = (int funcInt1) => (int) {
        int methodInt2 = 23;
        var addFunc2 = (int funcInt2) => (int) {
            int methodInt3 = 7;
            var addFunc3 = (int funcInt3) => (int) {
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
    var addFunc =  (int b) => (int) {
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
    return (int functionIntY) => (function (int) returns (int)) {
        return (int functionIntZ) => (int) {
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
    return (int y) => (int) {
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
    var out1stFunc = (int out1stParam) => (int) {
        int e = 45;
        int f = 45;
        var inner1Func = (int inner1Param) => (int) {
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
    var addFunc1 = (int funcInt1) => (int) {
        var addFunc2 = (int funcInt2) => (int) {
            var addFunc3 =  (int methodInt3, int methodInt2, int methodInt1, int funcInt3) => (int) {
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
    var addFunc = (int funcInt) => (int) {
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
    var addFunc = (int funcInt, float funcFloat) => (int) {
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
    return (float functionFloatY) => (function (boolean) returns (int)) {
        return (boolean functionBoolZ) => (int) {
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
    return (boolean b1, float b2) => (function () returns (int)) {
        var foo = () => (int) {
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
    return (string functionY) => (function (string) returns (string)) {
        return  (string functionZ) => (string) {
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
    var addFunc = (int funcInt) => (int) {
        return funcInt + methodInt + anotherMethodInt;
    };
    return addFunc;
}

function test15() returns int {
    var foo = basicTest();
    return foo(7);
}

function testClosureWithTupleTypes((string, float, string) g) returns (function (string, (string, float, string)) returns (string)){
    return (string x, (string, float, string) y) => (string) {
       var (i, j, k) = y;
       var (l, m, n) = g;
       return x + i + j + k + l + m + n;
    };
}

function test16() returns string {
    string a = "Hello";
    float b = 11.1;
    string c = "World !!!";
    var foo = testClosureWithTupleTypes((a, b, c));
    string i = "Ballerina";
    float j = 15.0;
    string k = "Program !!!";
    return foo("Im", (i, j, k));
}

function testClosureWithTupleTypesOrder((string, float, string) g) returns (function ((string, float, string), string) returns (string)){
    string i = "HelloInner";
    float j = 44.8;
    string k = "World Inner!!!";
    var r = (i, j, k);

    return ((string, float, string) y, string x) => (string) {
       var (a, b, c) = g;
       var (d, e, f) = y;
       var (i1, j1, k1) = r;

       return x + a + b + c + d + e + f + i1 + j1 + k1;
    };
}

function test17() returns string {
    string d = "Ballerina";
    float e = 15.0;
    string f = "Program!!!";

    string a = "Hello";
    float b = 11.1;
    string c = "World !!!";

    var foo = testClosureWithTupleTypesOrder((a, b, c));
    return foo((d, e, f), "I'm");
}

function globalVarAccessAndModifyTest() returns (int) {
    int a = 3;
    a = 6;
    globalA = 7;
    var addFunc = (int b) => (int) {
        return b + globalA + a;
    };
    return addFunc(3);
}

function test18() returns int {
    return globalVarAccessAndModifyTest();
}

type Person object {
    public {
        int age = 3,
        string name = "Hello Ballerina";
    }
    private {
        int year = 5;
        string month = "february";
    }

    function getAttachedFn() returns string {
        int b = 4;
        var foo = (float w) => (string) {
           return name + w + "K" + b + self.age;
        };
        return foo(7.4);
    }

    function getAttachedFP() returns function (float) returns (string) {
        int b = 4;
        var foo = (float w) => (string) {
            return w + self.year + b + "Ballerina !!!";
        };
        return foo;
    }

    public function externalAttachedFP() returns (function (float) returns (string));

};

public function Person::externalAttachedFP() returns (function (float) returns (string)) {
     int b = 4;
     var foo = (float w) => (string) {
        string d = w + "T" + b + self.year + self.name + self.age;
        return d;
     };
     return foo;
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

public function test21() returns (string) {
    Person p = new;
    var foo = p.externalAttachedFP();
    return foo(7.3);
}

function testDifferentArgs() returns (function (float) returns (function (float) returns (string))) {
    int outerInt = 4;
    boolean booOuter = false;
    var outerFoo = (float fOut) => (function (float) returns (string)) {
        int innerInt = 7;
        boolean booInner = true;
        var innerFoo = (float fIn) => (string) {
            string str = "Plain";
            if (!booOuter && booInner) {
                str = innerInt + "InnerInt" + outerInt + fOut + "InnerFloat" + fIn + "Ballerina !!!";
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
        int a = 4;
        b = a + b + <int>f;
    }

    var foo = (float f) => (string) {
        if (a > 8) {
            int a = 6;
            b = a + <int>f + b;
        }
        return "Ballerina" + b;
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
        int a = 4;
        b = a + b + <int>f;
    }

    var fooOut = (float f) => (function (float, boolean) returns (string)) {
        if (a > 8) {
            int a = 6;
            b = a + <int>f + b;
        }
        string s = "Out" + b;

        var fooIn = (float f, boolean boo) => (string) {
            if (a > 8 && !boo) {
                int a = 6;
                b = a + <int>f + b;
            }
            return s + "In" + b + "Ballerina!!!";
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
        int a = 4;
        b = a + b + <int>f;
    }

    var fooOutMost = (float f) => (function (float) returns (function (float, boolean) returns (string))) {
        if (a > 8) {
            int a = 6;
            b = a + <int>f + b;
        }
        string sOut = "OutMost" + b;

        var fooOut = (float f) => (function (float, boolean) returns (string)) {
            if (a == 9) {
                int a = 10;
                b = a + <int>f + b;
            }
            string s = sOut + "Out" + b;

            var fooIn = (float f, boolean boo) => (string) {
                if (a > 8 && !boo) {
                    int a = 12;
                    b = a + <int>f + b;
                }
                return s + "In" + b + "Ballerina!!!";
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

    var fooOutMost = (float f) => (function (float) returns (function (float, boolean) returns (string))) {
        int a = 8;
        string sOut = "OutMost" + b + a;

        var fooOut = (float f) => (function (float, boolean) returns (string)) {
            int a = 9;
            string s = sOut + "Out" + b + a;

            var fooIn = (float f, boolean boo) => (string) {
                int a = 10;
                b = a + <int>f + b;
                return s + "In" + b + "Ballerina!!!";
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
    float fadd = 0;
    float[] fa = [1.1, 2.2, -3.3, 4.4, 5.5];
    fa.foreach((float i) => { fadd = fadd + i;});
    float fsum = fadd;
    return (fsum);
}


