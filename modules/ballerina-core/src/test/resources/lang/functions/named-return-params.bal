function testNamedReturn(int a, string b) (int x, string s) {
    int p;
    string name;

    p = a + 2;
    name = b + ", john";

    x = p;
    s = name;

    return x, s;
}

function testNamedReturnDefaultValue() (int x, string s) {
    return x, s;
}