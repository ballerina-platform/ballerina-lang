function testDoubleValue() (double) {
    double b;
    b = 10.1d;
    return b;
}

function testNegativeDoubleValue() (double) {
    double y;
    y = -10.1d;
    return y;
}

function testDoubleValueAssignmentByReturnValue() (double) {
    double x;
    x = testDoubleValue();
    return x;
}

function testDoubleAddition() (double) {
    double b;
    double a;
    a = 9.9D;
    b = 10.1d;
    return a + b;
}

function testDoubleMultiplication() (double) {
    double b;
    double a;
    a = 2.5D;
    b = 5.5d;
    return a * b;
}

function testDoubleSubtraction() (double) {
    double b;
    double a;
    a = 25.5D;
    b = 15.5d;
    return a - b;
}

function testDoubleDivision() (double) {
    double b;
    double a;
    a = 25.5D;
    b = 5.1d;
    return a / b;
}

function testDoubleParameter(double a) (double) {
    double b;
    b = a;
    return b;
}

