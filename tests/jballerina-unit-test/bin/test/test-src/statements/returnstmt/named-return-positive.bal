function testSingleNamedReturnParam(int a, string b) returns (int) {
    int p = a + 2;
    return p;
}

function testSingleNamedReturnParamZeroReturnArgs(int a, string b) returns (int) {
    int p = a + 2;
    return p;
}

function testTwoNamedReturnParam(int a, string b) returns [int, string] {
    int p = a + 2;
    string name = b + ", john";

    return [p, name];
}

function testTwoNamedReturnParamZeroReturnArgs(int a, string b) returns [int, string] {
    int p = a + 2;
    string name = b + ", john";

    return [p,name];
}
