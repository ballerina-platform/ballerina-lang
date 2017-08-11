function testVarDeclarationWithAllIgnoredSymbols () (int) {
    float f = 10.0;
    var _, _ = <int>f;
    return 1;
}