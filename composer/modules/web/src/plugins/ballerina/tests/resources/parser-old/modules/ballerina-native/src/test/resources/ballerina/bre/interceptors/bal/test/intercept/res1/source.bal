package bal.test.intercept.res1;

import ballerina/lang.system;
import ballerina/lang.messages;

int i = 0;

function main (string... args) {
    system:println("Hello, World!");
}

function responseInterceptor (message msg) (boolean, message) {
    i = i + 1;
    string header = messages:getHeader(msg, "test") + " res1 (" + i + ")";
    messages:setHeader(msg, "test", header);
    return true, msg;
}