function testAsyncNonNativeBasic1() returns int {
    future<int> f1 = start add(5, 2);
    int result = wait f1;
    return result;
}

function add(int a, int b) returns int {
    return a + b;
}