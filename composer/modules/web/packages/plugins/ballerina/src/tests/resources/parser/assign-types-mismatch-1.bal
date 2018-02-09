function testAssignTypeMismatch() (int, string, int) {
    int a;
    string name;
    int b;

    a, name, b= testMultiReturnInternal();
    return a, name, b;
}

function testMultiReturnInternal() (string, string, int) {
    return 5, "john", 6;
}