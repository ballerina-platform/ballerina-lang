function testFunction() {
    future<int> fut = start startAction();
    int|error unionResult = wait fut;
    if unionResult is int {
        doSomething(unionResult);
    }
}

function startAction() returns int {
    return 0;
}

function doSomething(int a) {

}
