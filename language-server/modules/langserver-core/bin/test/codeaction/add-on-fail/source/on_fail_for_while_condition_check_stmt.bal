function testFunction() {
    while (check checkError()) {
        int i;
    }
}

function checkError() returns boolean|error {
    return error("Test Error");
}
