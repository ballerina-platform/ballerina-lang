function testAssignTypeMismatch() (int, string, int) {
    int a;
    int name;
    int b;

    a, name, b = testMultiReturnInternal();
    return a, name, b;
}

function testMultiReturnInternal() (int, string, int) {
    return 5, "john", 6;
}