function testFunction() {
    future<int> fut = start foo();
    int x = wait fut;
}

function foo() returns int {
    return 0;
}
