function testImplicitCastInvocation() returns (string) {

    int input = 7;
    string output = modifyInt(<string>input);
    return output;
}

function modifyInt(string a) returns (string) {
    string b = a + ".modified";
    return b;
}

function testImplicitCastInvocationWithMultipleParams() returns (string) {
    int a = 8;
    float b = 5;
    int c = 4;
    float d = 4;
    int e = 2;
    return multiParam(a, b, c, d, e);
}

function multiParam(int a, float b, int c, float d, int e) returns (string) {
    string result = "" + a + e;
    return result;
}



