function testIntegerValue() (int) {
    int b;
    b = 10;
    return b;
}

function testNegativeIntegerValue() (int) {
    int y;
    y = -10;
    return y;
}

function testIntegerValueAssignmentByReturnValue() (int) {
    int x;
    x = testIntegerValue();
    return x;
}

function testIntegerAddition() (int) {
    int b;
    int a;
    a = 9;
    b = 10;
    return a + b;
}

function testIntegerMultiplication() (int) {
    int b;
    int a;
    a = 2;
    b = 5;
    return a * b;
}

function testIntegerSubtraction() (int) {
    int b;
    int a;
    a = 25;
    b = 15;
    return a - b;
}

function testIntegerDivision() (int) {
    int b;
    int a;
    a = 25;
    b = 5;
    return a / b;
}

function testIntegerParameter(int a) (int) {
    int b;
    b = a;
    return b;
}

