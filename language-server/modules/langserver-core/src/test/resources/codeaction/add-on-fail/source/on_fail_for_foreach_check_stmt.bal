function testFunction() {
    var testList = [];
    foreach var item in testList {
        int i = check checkError();
    } 
}

function checkError() returns int|error {
    return error("Test Error");
}
