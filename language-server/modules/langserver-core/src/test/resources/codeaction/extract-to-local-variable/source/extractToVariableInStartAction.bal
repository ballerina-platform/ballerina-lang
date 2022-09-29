function testFunction() {
    future<int> fut = start startAction();
    int|error unionResult = wait fut;
}

function startAction() returns int {
    return 0;
}
