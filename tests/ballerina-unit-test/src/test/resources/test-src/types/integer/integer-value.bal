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
    return a + b;
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
    return a * b;
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
    int c = 10;
    return (a - b) - c;
}

function testIntegerDivision () returns (int) {
    int b;
    int a;
    a = 25;
    b = 5;
    return a / b;
}

function testIntegerTypesDivision () returns (int) {
    int b = 100;
    int a = 0xa;
    int c = 10;
    return (b / a) / c;
}

function testIntegerParameter (int a) returns (int) {
    int b;
    b = a;
    return b;
}
