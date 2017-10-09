function testVarDeclarationWithAllDeclaredSymbols () (int, TypeConversionError) {
    int a;
    TypeConversionError err;
    float f = 10.0;
    var a, err = <int>f;
    return a, err;
}