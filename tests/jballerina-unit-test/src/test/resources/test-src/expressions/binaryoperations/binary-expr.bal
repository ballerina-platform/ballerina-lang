function makeChild(boolean stone, boolean value) returns (boolean) {
    boolean result = false;
    // stone and valuable
    if (stone && value) {
        result = true;
        // same as above
    } else if (value && stone) {
        result = true;
    } else if (value || !stone) {
        result =  false;
        // not stone or valuable
    } else if (!stone || !value) {
        result =  false;
    } else if (stone || !value) {
        result =  false;
    }
    return result;
}

function multiBinaryANDExpression(boolean one, boolean two, boolean three) returns (boolean) {
    return one && two && three;
}

function multiBinaryORExpression(boolean one, boolean two, boolean three) returns (boolean) {
    return one || two || three;
}

function multiBinaryExpression(boolean one, boolean two, boolean three) returns (boolean) {
    return (!one || (two && three)) || (!three || (one && two));
}

function bitwiseAnd(int a, int b, byte c, byte d) returns [int, byte, byte, byte] {
    [int, byte, byte, byte] res = [0, 0, 0, 0];
    res[0] = a & b;
    res [1] = a & c;
    res [2] = c & d;
    res [3] = b & d;
    return res;
}

function testLengthyBinaryExpressionsForOOM() {

    decimal d1 = 1;
    decimal d2 = 1;
    decimal d3 = 0;
    decimal d4 = 0;
    decimal d5 = 1;
    decimal d6 = 0;
    decimal d7 = 0;
    decimal d8 = 0;
    decimal d9 = 1;
    decimal d10 = 0;
    decimal d11 = 0;
    decimal d12 = 0;
    decimal d13 = 1;
    decimal d14 = 0;
    decimal d15 = 0;
    decimal d16 = 0;
    decimal d17 = 0;
    decimal d18 = 0;
    decimal d19 = 0;
    decimal d20 = 0;
    decimal d21 = 0;
    decimal d22 = 0;
    decimal d23 = 0;
    decimal d24 = 0;

    decimal totalOtherCost = + d1
                             + d2
                             + d3
                             + d4
                             + d5
                             + d6
                             + d7
                             + d8
                             + d9
                             + d10
                             + d11
                             + d12
                             + d13
                             + d14
                             + d15
                             + d16
                             + d17
                             + d18
                             + d19
                             + d20
                             + d21
                             + d22
                             + d23
                             + d24
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 0
                             + 1
                             + 1
                             + 1
                             + 1
                             + 1;
    assertEquality(totalOtherCost, 10d);
}

function assertEquality(any actual, any expected) {
    if actual is anydata && expected is anydata && actual == expected {
        return;
    }
    panic error("expected '" + expected.toString() + "', found '" + actual.toString() + "'");
}
