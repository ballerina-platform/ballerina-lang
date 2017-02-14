const int a = 5;

const string b = "A" + "B";

const int n = -2;

const int sum = foo(5, 4);

const boolean result = true && false;

const double d = 5.123456d + 1.8d;

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

function foo(int a, int b) (int) {
    return a + b + a * b;
}