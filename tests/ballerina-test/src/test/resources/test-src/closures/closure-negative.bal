import ballerina/io;

function getStringFunc1(string functionX) returns (function (string) returns (function (string) returns (string))) {
    return (string functionY) => (function (string) returns (string)) {
        return (string functionZ) => (string) {
            return functionY + functionX + functionZ + functionR;
        };
    };
}

function threeLevelTest() returns (int) {
    var addFunc1 = (int funcInt1) => (int) {
        var addFunc2 = (int funcInt2) => (int) {
            var addFunc3 = (int methodInt3, int methodInt2, int methodInt1, int funcInt3) => (int) {
                return funcInt3 + methodInt1 + methodInt2 + methodInt3;
            };
            return addFunc3(7, 23, 2, 8) + methodInt3;
        };
        return addFunc2(4) + funcInt1;
    };
    return addFunc1(6);
}

function testDifferentArgs(int a) returns (function (float) returns (function (float) returns (string))) {
    int outerInt = 4;
    boolean booOuter = false;
    var outerFoo = (float fOut) => (function (float) returns (string)) {
        int innerInt = 7;
        a = 56;
        boolean booInner = true;
        var innerFoo = (float fIn) => (string) {
            string str = "Plain";
            if (!booOuter && booInner) {
                fOut = 4.6;
                str = innerInt + "InnerInt" + outerInt + fOut + "InnerFloat" + fIn + "Ballerina !!!";
            }
            return str;
        };
        return innerFoo;
    };
    return outerFoo;
}

function testVariableShadowingInClosure(int a) returns function (float) returns (function (float, boolean) returns (string)){
    int b = 4;
    float f = 5.6;
    boolean boo = true;

    if (a < 10) {
        int a = 4;
        b = a + b + <int>f;
    }

    var fooOut = (float f) => (function (float, boolean) returns (string)) {
        if (a > 8) {
            a = 6;
            boo = false;
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
