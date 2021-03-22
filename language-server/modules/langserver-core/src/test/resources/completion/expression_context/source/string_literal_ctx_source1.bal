import ballerina/module1;

function testFunction() {
    module1:Client cl = new("http:");
}

function testFunctionWithParams(int a, int b) returns int {
    return a+b;
}
