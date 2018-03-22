package samples.functionTest;

import ballerina/lang.system;


function main (string[] args) {
    int c = intAdd(5, 3);
    system:println("addintion is : " + c);
}


function intAdd(int a, int b) (int) {
    return a + b;
}

function intSubtract(int a, int b) (int) {
    return a - b;
}

function floatAdd(float a, float b) (float) {
    return a + b;
}

function floatSubtract(float a, float b) (float) {
    return a - b;
}

function stringConcat(string a, string b) (string) {
    return a + b;
}

function stringAndIntConcat(string a, int b) (string) {
    return a + b;
}
