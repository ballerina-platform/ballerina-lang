function testIntegerValue () returns (int) {
    int b;
    b = 10;
    return b;
}

function testNegativeIntegerValue () returns (int) {
    int y;
    y = -10;
    return y;
}

function testHexValue () returns (int) {
    int b;
    b = 0xa;
    return b;
}

function testNegativeHaxValue () returns (int) {
    int b;
    b = -0xa;
    return b;
}

function testOctalValue () returns (int) {
    int b;
    b = 0_12;
    return b;
}

function testNegativeOctalValue () returns (int) {
    int b;
    b = -0_12;
    return b;
}

function testBinaryValue () returns (int) {
    int b;
    b = 0b1010;
    return b;
}

function testNegativeBinaryValue () returns (int) {
    int b;
    b = -0b1010;
    return b;
}

function testIntegerValueAssignmentByReturnValue () returns (int) {
    int x;
    x = testIntegerValue();
    return x;
}

function testIntegerAddition () returns (int) {
    int b;
    int a;
    a = 9;
    b = 10;
    return a + b;
}

function testIntegerTypesAddition () returns (int) {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    return a + b + c + d;
}


function testIntegerMultiplication () returns (int) {
    int b;
    int a;
    a = 2;
    b = 5;
    return a * b;
}

function testIntegerTypesMultiplication () returns (int) {
    int b = 1;
    int a = 0x1;
    int c = 0_1;
    int d = 0b1;
    return a * b * c * d;
}

function testIntegerSubtraction () returns (int) {
    int b;
    int a;
    a = 25;
    b = 15;
    return a - b;
}

function testIntegerTypesSubtraction () returns (int) {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    return (a - b) - (c - d);
}

function testIntegerDivision () returns (int) {
    int b;
    int a;
    a = 25;
    b = 5;
    return a / b;
}

function testIntegerTypesDivision () returns (int) {
    int b = 10;
    int a = 0xa;
    int c = 0_12;
    int d = 0b1010;
    return (a / b) / (c / d);
}

function testIntegerParameter (int a) returns (int) {
    int b;
    b = a;
    return b;
}
