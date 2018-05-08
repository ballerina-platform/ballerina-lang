
import ballerina/lang.messages;
import ballerina/lang.system;

function requestInterceptor (message msg) (boolean, message) {
    string header = "req1";
    messages:setHeader(msg, "test", header);
    return true, msg;
}

function main (string... args) {
    system:println("Hello, World!");
}