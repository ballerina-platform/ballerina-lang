import ballerina/http;

function myFunction(int a, int b, string c) {
    myFunction(a, b)
}

function innerFunction(int a, int b) returns int {
    return 1;
}