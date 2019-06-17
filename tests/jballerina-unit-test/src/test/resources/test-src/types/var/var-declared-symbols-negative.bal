function testVarDeclarationWithAllDeclaredSymbols () returns [int, string] {
    int a;
    string s;
    var [a, s] = unionReturnTest();
    return [a, s];
}

function unionReturnTest() returns [int, string] {
    return [5, "hello"];
}

function testVarDeclarationWithAtLeaseOneNonDeclaredSymbol () returns [int, error] {
    int a;
    var [a, err] = returnTupleForVarAssignment();
    return [a, err];
}

function returnTupleForVarAssignment() returns [int, error] {
    int a = 10;
    error er = error("");
    return [a, er];
}
