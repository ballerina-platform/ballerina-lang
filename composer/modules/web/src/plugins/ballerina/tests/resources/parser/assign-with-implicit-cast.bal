function testIntCastFloatStmt(int a) (float) {
    float x;
    //Cannot directly assign int to float, need to convert
    x = <float> a;
    return x;
}

function testBinaryExpressionIntAndFloatStmt(int a) (float) {
    float x;
    x = <float> a;
    return x + a;
}
