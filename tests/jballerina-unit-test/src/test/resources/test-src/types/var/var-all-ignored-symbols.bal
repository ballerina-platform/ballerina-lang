function testVarDeclarationWithAllIgnoredSymbols () returns string {
    string s = "10";
    [_, _] = unionReturnTest();
    return "success";
}

function unionReturnTest() returns [int, string] {
    return [5, "hello"];
}
