
function testFunction() {
    future<string> fs = start getStringResult();
    wait {}
}

function getStringResult() returns string {
    return "Hello World";
}
