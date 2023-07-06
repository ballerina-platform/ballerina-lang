function testFunction() {
    while true {
        int i = check checkError();
    }
}

function checkError() returns int|error {
    return error("Test Error");
}
