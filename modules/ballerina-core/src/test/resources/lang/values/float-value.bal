function testFloatValue() (float) {
    float b;
    b = 10.1;
    return b;
}

function testNegativeFloatValue() (float) {
    float y;
    y = -10.1;
    return y;
}

function testFloatValueAssignmentByReturnValue() (float) {
    float x;
    x = testFloatValue();
    return x;
}

function testFloatAddition() (float) {
    float b;
    float a;
    a = 9.9;
    b = 10.1;
    return a + b;
}

function testFloatMultiplication() (float) {
    float b;
    float a;
    a = 2.5;
    b = 5.5;
    return a * b;
}

function testFloatSubtraction() (float) {
    float b;
    float a;
    a = 25.5;
    b = 15.5;
    return a - b;
}

function testFloatDivision() (float) {
    float b;
    float a;
    a = 25.5;
    b = 5.1;
    return a / b;
}

function testFloatParameter(float a) (float) {
    float b;
    b = a;
    return b;
}

