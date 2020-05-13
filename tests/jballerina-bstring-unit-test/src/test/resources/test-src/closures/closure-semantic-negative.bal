

function getStringFunc1(string functionX) returns (function (string) returns (function (string) returns (string))) {
    return function (string functionY) returns (function (string) returns (string)) {
        return function (string functionZ) returns (string) {
            return functionY + functionX + functionZ + functionR;
        };
    };
}

function threeLevelTest() returns (int) {
    var addFunc1 = function (int funcInt1) returns (int) {
        var addFunc2 = function (int funcInt2) returns (int) {
            var addFunc3 = function (int methodInt3, int methodInt2, int methodInt1, int funcInt3) returns (int) {
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
    var outerFoo = function (float fOut) returns (function (float) returns (string)) {
        int innerInt = 7;
        a = 56;
        boolean booInner = true;
        var innerFoo = function (float fIn) returns (string) {
            string str = "Plain";
            if (!booOuter && booInner) {
                fOut = 4.6;
                str = innerInt.toString() + "InnerInt" + outerInt.toString() + fOut.toString() + "InnerFloat" + fIn.toString() + "Ballerina !!!";
            }
            return str;
        };
        return innerFoo;
    };
    return outerFoo;
}

function testVariableShadowingInClosure(int a) returns function (float) returns (function (float, boolean) returns (string)) {
    int b = 4;
    float f = 5.6;
    boolean boo = true;

    if (a < 10) {
        int a = 4;
        b = a + b + <int>f;
    }

    var fooOut = function (float f) returns (function (float, boolean) returns (string)) {
        if (a > 8) {
            a = 6;
            boo = false;
            b = a + <int>f + b;
        }
        string s = "Out" + b.toString();

        var fooIn = function (float f, boolean boo) returns (string) {
            if (a > 8 && !boo) {
                int a = 6;
                b = a + <int>f + b;
            }
            return s + "In" + b.toString() + "Ballerina!!!";
        };
        return fooIn;
    };
    return fooOut;
}

function testClosureScopingNegative() returns int {
    int i = 34;
    var addFunc1 = function (int a) returns (int) {
        int j = 23;
        var addFunc2 = function (float b) returns (int) {
            int k = 45;
            var addFunc3 = function (float c) returns (int) {
                return a + <int>b + <int>c + i + j + k + l + m + n;
            };
            int l = 2;
            return addFunc3(2.3) + l + m + n;
        };
        int m = 6;
        return addFunc2(4.2) + m + n;
    };
    int n = 2;
    return addFunc1(6);
}

int p = 2;

function() foo = function () returns () {
    int i = 34;
    var addFunc1 = function (int a) returns (int) {
        return a + p + m + i;
    };
    int m = 3;
    int k = addFunc1(6);
};

//
//function testUninitializedClosureVars() {
//    string a;
//
//    var bazz = function () {
//        string str = a + "aa";
//    };
//
//    bazz();
//
//    string b;
//    int count;
//
//    var bar = function () {
//        count += 10;
//
//        b = b + "bb";
//    };
//
//    bar();
//}
