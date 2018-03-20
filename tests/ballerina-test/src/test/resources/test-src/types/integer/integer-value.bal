function testIntegerValue () (int) {
    int b;
    b = 10;
    return b;
}

function testNegativeIntegerValue () (int) {
    int y;
    y = -10;
    return y;
}

function testHexValue () (int) {
    int b;
    b = 0xa;
    return b;
}

function testNegativeHaxValue () (int) {
    int b;
    b = -0xa;
    return b;
}

function testOctalValue () (int) {
    int b;
    b = 0_12;
    return b;
}

function testNegativeOctalValue () (int) {
    int b;
    b = -0_12;
    return b;
}

function testBinaryValue () (int) {
    int b;
    b = 0b1010;
    return b;
}

function testNegativeBinaryValue () (int) {
    int b;
    b = -0b1010;
    return b;
}

function testIntegerValueAssignmentByReturnValue () (int) {
    int x;
    x = testIntegerValue();
    return x;
}

function testIntegerAddition () (int) {
    int b;
    int a;
    a = 9;
    b = 10;
    return a + b;
}

function testIntegerTypesAddition () (int) {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    return a + b + c + d;
}


function testIntegerMultiplication () (int) {
    int b;
    int a;
    a = 2;
    b = 5;
    return a * b;
}

function testIntegerTypesMultiplication () (int) {
    int b = 1;
    int a = 0x1;
    int c = 0_1;
    int d = 0b1;
    return a * b * c * d;
}

function testIntegerSubtraction () (int) {
    int b;
    int a;
    a = 25;
    b = 15;
    return a - b;
}

function testIntegerTypesSubtraction () (int) {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    return (a - b) - (c - d);
}

function testIntegerDivision () (int) {
    int b;
    int a;
    a = 25;
    b = 5;
    return a / b;
}

function testIntegerTypesDivision () (int) {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    return (a / b) / (c / d);
}

function testIntegerParameter (int a) (int) {
    int b;
    b = a;
    return b;
}
