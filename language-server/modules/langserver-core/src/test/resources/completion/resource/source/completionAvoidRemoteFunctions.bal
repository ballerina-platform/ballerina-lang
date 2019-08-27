import ballerina/http;

service serviceName on new http:Listener(8080) {
    resource function newResource(http:Caller caller, http:Request request) {
        int var1 = 12;
        caller.
    }
}