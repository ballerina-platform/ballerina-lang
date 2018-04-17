package functionTest;

import ballerina/io;


function main (string... args) {
    int c = intAdd(5, 3);
    io:println("addintion is : " + c);
}


function intAdd(int a, int b) returns (int) {
    return (a + b);
}

function intSubtract(int a, int b) returns (int) {
    return (a - b);
}

function floatAdd(float a, float b) returns (float) {
    return (a + b);
}

function floatSubtract(float a, float b) returns (float) {
    return (a - b);
}

function stringConcat(string a, string b) returns (string) {
    return (a + b);
}

function stringAndIntConcat(string a, int b) returns (string) {
    return (a + b);
}
