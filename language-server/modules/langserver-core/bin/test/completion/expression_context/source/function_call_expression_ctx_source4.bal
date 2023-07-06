import ballerina/module1;

function testFunction() {
    int valueA = 123;
    int valueB = 456; 
    testFunctionWithParams(valueA, v)
}

function testFunctionWithParams(int a, int b) returns int {
    return a+b;
}
