function testVarDeclarationWithAllDeclaredSymbols () (int, error) {
    int a;
    error err;
    string s = "10";
    var a, err = <int>s;
    return a, err;
}