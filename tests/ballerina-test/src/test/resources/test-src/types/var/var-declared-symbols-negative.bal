function testVarDeclarationWithAllDeclaredSymbols () returns (int, string) {
    int a;
    string s;
    var (a, s) = unionReturnTest();
    return (a, s);
}

function unionReturnTest() returns (int, string) {
    return (5, "hello");
}