function testAssignCountMismatch() (int, string, int) {
    int a;
    string name;
    int b;
    int c;

    a, name, b, c = testMultiReturnInternal();
    return a, name, b;
}

function testMultiReturnInternal() (int, string, int) {
    return 5, "john", 6;
}