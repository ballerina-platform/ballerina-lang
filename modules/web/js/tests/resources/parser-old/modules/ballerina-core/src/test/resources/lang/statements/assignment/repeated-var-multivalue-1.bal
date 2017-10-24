function testMultiReturn() (int, string, int) {
    var a, name, a = testMultiReturnInternal();
    return a, name, b;
}

function testMultiReturnInternal() (int, string, int) {
    return 5, "john", 6;
}