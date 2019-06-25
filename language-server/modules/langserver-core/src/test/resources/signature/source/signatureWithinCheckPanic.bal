import ballerina/http;
service greet on new http:Listener(8080) {
    resource function hi(http:Caller caller, http:Request request) {
        checkpanic caller->respond()
    }
}