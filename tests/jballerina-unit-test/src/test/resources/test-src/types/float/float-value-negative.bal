function testInvlaidFloatValue() returns (float) {
    float b;
    b = 010.1;
    return b;
}

function testInvlaidFloatValue2() {
    float b = 999e9999999999;
    float c = 999e-9999999999;
    decimal|float d = 999e9999999999;
    int|decimal|float e = 99.9E99999999;
    int|decimal|float f = 99.9E-99999999;
}

type Bar 0x9999999p999999999999999999999999;

type Bar1 0x9999999p-999999999999999999999999;

type Baz 9999999999e9999999999999999999f;

type Baz1 9999999999e-9999999999999999999f;

0x999.9p999999999999999 x = 0x999.9p999999999999999;
