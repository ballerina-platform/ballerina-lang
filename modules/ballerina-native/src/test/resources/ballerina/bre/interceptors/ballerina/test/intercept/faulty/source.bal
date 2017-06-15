package ballerina.test.intercept.faulty;

import ballerina.lang.system;

function requestInterceptor (message msg) (boolean, message) {
    string[] a;
    a[5] = 10;
    return true, msg;
}

function responseInterceptor (message msg) (boolean, message) {
    return false, null;
}

function main (string[] args) {
    system:println("Hello, World!");
}