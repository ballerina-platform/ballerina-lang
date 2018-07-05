function testVarDeclarationWithAllIgnoredSymbols () returns (int) {
    string s = "10";
    var (_, _) = unionReturnTest();
    return 1;
}

function unionReturnTest() returns (int, string) {
    return (5, "hello");
}