const int a = 5;

const string b = "A" + "B";

const int n = -2;

const int sum = foo(5, 4);

const boolean result = true && false;

const double d = 5.123456d + 1.8d;

const string implCast = 10;

const int expCast = (int) "10";

function testIntA() (int) {
    return a;
}

function testStringB() (string) {
    return b;
}

function testSum() (int) {
    return sum;
}

function testResult() (boolean) {
    return result;
}

function testDouble() (double) {
    return d;
}

function testImplCast() (string) {
    return  implCast;
}

function testExpCast() (int) {
    return  expCast;
}

function foo(int a, int b) (int) {
    return a + b + a * b;
}