
function testFunction() {
    future<string> fs = start getStringResult();
    wait {f};
}

function getStringResult() returns string {
    return "Hello World";
}
