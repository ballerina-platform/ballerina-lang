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
