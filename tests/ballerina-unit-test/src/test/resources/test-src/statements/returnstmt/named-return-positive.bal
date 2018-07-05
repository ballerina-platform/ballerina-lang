function testSingleNamedReturnParam(int a, string b) returns (int) {
    int p;
    p = a + 2;
    return p;
}

function testSingleNamedReturnParamDefaultValue(int a, string b) returns (int) {
    int p;
    return p;
}

function testSingleNamedReturnParamZeroReturnArgs(int a, string b) returns (int) {
    int p;
    p = a + 2;
    return p;
}

function testSingleNamedReturnParamDefaultValueZeroReturnArgs(int a, string b) returns (int) {
    int p;
    return p;
}


function testTwoNamedReturnParam(int a, string b) returns (int, string) {
    int p;
    string name;

    p = a + 2;
    name = b + ", john";

    return (p, name);
}

function testTwoNamedReturnParamDefaultValue() returns (int, string) {
    int p;
    string name;
    return (p, name);
}

function testTwoNamedReturnParamZeroReturnArgs(int a, string b) returns (int, string) {
    int p;
    string name;

    p = a + 2;
    name = b + ", john";

    return (p,name);
}

function testTwoNamedReturnParamZeroReturnArgsDefaultValue() returns (int, string) {
    int p;
    string name;
    return (p,name);
}
