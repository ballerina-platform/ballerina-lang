package ballerina.test.intercept.faulty;

import ballerina.lang.system;

function requestInterceptor (message msg) (boolean, message) {
    string[] a;
    a[5] = 10;
    return true, msg;
}

function main (string[] args) {
    system:println("Hello, World!");
}