function testVarDeclarationWithAllDeclaredSymbols () (int, TypeConversionError) {
    int a;
    TypeConversionError err;
    string s = "10";
    var a, err = <int>s;
    return a, err;
}