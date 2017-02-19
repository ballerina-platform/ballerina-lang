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

function testBinaryExpressionIntAndFloatStmt(int a) (float) {
    float x;
    x = a;
    return x + a;
}