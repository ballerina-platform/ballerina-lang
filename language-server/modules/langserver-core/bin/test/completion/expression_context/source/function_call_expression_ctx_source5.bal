import ballerina/module1;

function testFunction() {
    int valueA = 123;
    int valueB = 456; 
    testFunctionWithParams(valueA, module1:)
}

function testFunctionWithParams(int a, int b) returns int {
    return a+b;
}
