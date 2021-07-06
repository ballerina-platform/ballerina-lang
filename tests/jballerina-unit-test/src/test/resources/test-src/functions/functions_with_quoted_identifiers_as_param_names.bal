import ballerina/test;

function funcWithQuotedSelfAsParamName(int 'self) returns int {
    return 'self + 5;
}

function funcWithQuotedIntAsParamName(string 'int) returns string {
    return 'int + "team!";
}

function testFuncWithQuotedIdentifiersAsParameterName() {
    int i = funcWithQuotedSelfAsParamName(2);
    test:assertEquals(i, 7);

    string str = funcWithQuotedIntAsParamName("Hello ");
    test:assertEquals(str, "Hello team!");
}


