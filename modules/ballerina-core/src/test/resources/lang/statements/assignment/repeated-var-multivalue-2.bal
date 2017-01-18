function testMultiReturn() (int, string, int) {
    int a;
    string name;
    int b;

    a, name, name = testMultiReturnInternal();
    return a, name, b;
}

function testMultiReturnInternal() (int, string, int) {
    return 5, "john", 6;
}