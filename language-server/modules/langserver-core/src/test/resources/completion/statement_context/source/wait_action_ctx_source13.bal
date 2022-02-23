import ballerina/module1;

function testFunction() {
    future<string> fs = start getStringResult();
    future<string> fs2 = start getStringResult2();
    worker W1 {
        int workerVar = 12;
    }
    wait W1|module1:
}

function getStringResult() returns string {
    return "Hello World";
}

function getStringResult2() returns string {
    return "Hello World2";
}
