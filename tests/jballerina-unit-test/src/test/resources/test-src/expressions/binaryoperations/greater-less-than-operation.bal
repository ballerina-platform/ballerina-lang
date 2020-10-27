import ballerina/test;

function testIntRanges(int a) returns (int) {
    int retunType = 0;
    if (a <= 0) {
        retunType = 1;
    } else if ((a > 0) && (a < 100)) {
        retunType = 2;
    } else if (a >= 100) {
        retunType = 3;
    }
    return retunType;
}

function testFloatRanges(float a) returns (int) {
    int retunType = 0;
    if (a <= 0.0) {
        retunType = 1;
    } else if ((a > 0.0) && (a < 100.0)) {
        retunType = 2;
    } else if (a >= 101.0) {
        retunType = 3;
    }
    return retunType;
}

function testIntAndFloatCompare(int a, float b) returns (boolean) {
    return a > b;
}

function intGTFloat(int a, float b) returns (boolean) {
    return a > b;
}

function floatGTInt(float a, int b) returns (boolean) {
    return a > b;
}

function intLTFloat(int a, float b) returns (boolean) {
    return a < b;
}

function floatLTInt(float a, int b) returns (boolean) {
    return a < b;
}

function intLTEFloat(int a, float b) returns (boolean) {
    return a <= b;
}

function floatLTEInt(float a, int b) returns (boolean) {
    return a <= b;
}

function intGTEFloat(int a, float b) returns (boolean) {
    return a >= b;
}

function floatGTEInt(float a, int b) returns (boolean) {
    return a >= b;
}

function testDecimalComparison() {
    decimal lowValue = 3;
    decimal highValue = 5;

    test:assertTrue(lowValue < highValue);
    test:assertTrue(lowValue <= highValue);

    test:assertTrue(highValue > lowValue);
    test:assertTrue(highValue >= lowValue);

    test:assertFalse(highValue < lowValue);
    test:assertFalse(highValue <= lowValue);
    test:assertFalse(lowValue > highValue);
    test:assertFalse(lowValue >= highValue);

    test:assertFalse(lowValue > lowValue);
    test:assertFalse(lowValue < lowValue);
    test:assertTrue(lowValue >= lowValue);
    test:assertTrue(lowValue <= lowValue);
}
