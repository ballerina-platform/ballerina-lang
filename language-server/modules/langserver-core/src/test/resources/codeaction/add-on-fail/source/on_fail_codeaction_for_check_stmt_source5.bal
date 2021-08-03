function testFunction() {
    testFunction1(check checkError());
}

function checkError() returns int|error {
    return error("Test Error");
}

function testFunction1(int x) {
    return;
}
