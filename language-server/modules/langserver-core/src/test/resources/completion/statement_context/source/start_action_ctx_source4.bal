
function testFunction() {
    future<string> fs = @strand {thread: "any"} start 
}

function getStringResult() returns string {
    return "Hello World";
}
