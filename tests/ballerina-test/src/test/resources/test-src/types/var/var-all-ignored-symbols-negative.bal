function testVarDeclarationWithAllIgnoredSymbols () returns (int) {
    string s = "10";
    var _, _ = <int> s;
    return 1;
}