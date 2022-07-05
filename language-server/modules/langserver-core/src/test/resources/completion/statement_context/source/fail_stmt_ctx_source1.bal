function testFunction() {
    int testInt = 1;
    error testErr = error("test error");
    do {
        fail ;
    }
}

function getIntOrError() returns error|int {
    return error("");
}

function getError() returns error {
    return error("");
}
