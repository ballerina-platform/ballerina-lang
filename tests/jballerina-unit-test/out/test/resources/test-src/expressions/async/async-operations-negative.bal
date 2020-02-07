function testAsyncNegative1() returns int {
    int f2 = 4;
    int result = wait f2;
    return result;
}

function testAsyncNegative2() returns int {
    var f2 = 4;
    int result = wait f2;
    return result;
}

function testAsyncNegative3() returns int {
    any f2 = 4;
    int result = wait f2;
    return result;
}
