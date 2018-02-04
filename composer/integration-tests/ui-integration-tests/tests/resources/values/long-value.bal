function testLongValue() (long) {
    long b;
    b = 10l;
    return b;
}

function testNegativeLongValue() (long) {
    long y;
    y = -10l;
    return y;
}

function testLongValueAssignmentByReturnValue() (long) {
    long x;
    x = testLongValue();
    return x;
}

function testLongAddition() (long) {
    long b;
    long a;
    a = 9L;
    b = 10L;
    return a + b;
}

function testLongMultiplication() (long) {
    long b;
    long a;
    a = 2L;
    b = 5l;
    return a * b;
}

function testLongSubtraction() (long) {
    long b;
    long a;
    a = 25L;
    b = 15l;
    return a - b;
}

function testLongDivision() (long) {
    long b;
    long a;
    a = 25L;
    b = 5l;
    return a / b;
}

function testLongParameter(long a) (long) {
    long b;
    b = a;
    return b;
}

