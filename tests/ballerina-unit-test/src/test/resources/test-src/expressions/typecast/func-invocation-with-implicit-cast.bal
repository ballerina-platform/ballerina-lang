function testImplicitCastInvocation() returns (string) {

    int input = 7;
    string output = modifyInt(<string>input);
    return output;
}

function modifyInt(string a) returns (string) {
    string b = a + ".modified";
    return b;
}
