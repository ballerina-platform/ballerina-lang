function testSingleNamedReturnParam(int a, string b) (int x) {
    int p;
    p = a + 2;
    x = p;
    return x;
}

function testSingleNamedReturnParamDefaultValue(int a, string b) (int x) {
    return;
}

function testSingleNamedReturnParamZeroReturnArgs(int a, string b) (int x) {
    int p;
    p = a + 2;
    x = p;
    return;
}

function testSingleNamedReturnParamDefaultValueZeroReturnArgs(int a, string b) (int x) {
    return;
}


function testTwoNamedReturnParam(int a, string b) (int x, string s) {
    int p;
    string name;

    p = a + 2;
    name = b + ", john";

    x = p;
    s = name;

    return x, s;
}

function testTwoNamedReturnParamDefaultValue() (int x, string s) {
    return x, s;
}

function testTwoNamedReturnParamZeroReturnArgs(int a, string b) (int x, string s) {
    int p;
    string name;

    p = a + 2;
    name = b + ", john";

    x = p;
    s = name;

    return;
}

function testTwoNamedReturnParamZeroReturnArgsDefaultValue() (int x, string s) {
    return;
}