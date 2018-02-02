function testIntCastLongStmt(int a) (long) {
    long x;
    x = a;
    return x;
}

function testIntCastFloatStmt(int a) (float) {
    float x;
    x = a;
    return x;
}

function testIntCastDoubleStmt(int a) (double) {
    double x;
    x = a;
    return x;
}

function testLongCastFloatStmt(long a) (float) {
    float x;
    x = a;
    return x;
}

function testLongCastDoubleStmt(long a) (double) {
    double x;
    x = a;
    return x;
}

function testFloatCastDoubleStmt(float a) (double) {
    double x;
    x = a;
    return x;
}

function testBinaryExpressionIntAndLongStmt(int a) (long) {
    long x;
    x = a;
    return x + a;
}

function testBinaryExpressionIntAndFloatStmt(int a) (float) {
    float x;
    x = a;
    return x + a;
}

function testBinaryExpressionIntAndDoubleStmt(int a) (double) {
    double x;
    x = a;
    return x + a;
}

function testBinaryExpressionLongAndFloatStmt(long a) (float) {
    float x;
    x = a;
    return x + a;
}

function testBinaryExpressionLongAndDoubleStmt(long a) (double) {
    double x;
    x = a;
    return x + a;
}

function testBinaryExpressionFloatAndDoubleStmt(float a) (double) {
    double x;
    x = a;
    return x + a;
}