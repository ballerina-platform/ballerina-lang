function testVarDeclarationWithRedeclaredSymbol () (int) {
    int a = 5;
    float f = 10.0;
    var a, err = <int>f;
    return a;
}