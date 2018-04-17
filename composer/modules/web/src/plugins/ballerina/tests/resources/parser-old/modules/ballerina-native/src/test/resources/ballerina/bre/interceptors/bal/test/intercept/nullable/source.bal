package bal.test.intercept.nullable;

import ballerina/lang.system;

function responseInterceptor (message msg) (boolean, message) {
    return false, null;
}

function main (string... args) {
    system:println("Hello, World!");
}