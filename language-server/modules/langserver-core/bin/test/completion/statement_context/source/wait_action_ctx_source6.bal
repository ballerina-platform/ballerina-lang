
function testFunction() {
    future<string> fs = start getStringResult();
    future<string> fs2 = start getStringResult2();
    wait {f1: }
}

function getStringResult() returns string {
    return "Hello World";
}

function getStringResult2() returns string {
    return "Hello World2";
}
