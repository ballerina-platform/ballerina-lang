function testInvlaidFloatValue() returns (float) {
    float b;
    b = 010.1;
    return b;
}

function testInvlaidFloatValue2() returns (float) {
    float b = 999e9999999999;
    float c = 999e-9999999999;
    decimal|float d = 999e9999999999;
    int|decimal|float e = 99.9E99999999;
    int|decimal|float f = 99.9E-99999999;
}