import ballerina/http;

function myFunction(int a, int b, string c) {
    myFunction(a, innerFunction(a, ));
}

function innerFunction(int a, int b) returns int {
    return 1;
}