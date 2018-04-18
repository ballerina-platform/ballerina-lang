package bal.test.intercept.res2;

import ballerina/lang.system;
import ballerina/lang.messages;

function main (string... args) {
    system:println("Hello, World!");
}

function responseInterceptor (message msg) (boolean, message) {
    string header = messages:getHeader(msg, "test") + " res2";
    messages:setHeader(msg, "test", header);
    return true, msg;
}