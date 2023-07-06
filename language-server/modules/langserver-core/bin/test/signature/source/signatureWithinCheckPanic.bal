import ballerina/httpx;
service greet on new httpx:Listener(8080) {
    resource function hi(httpx:Caller caller, httpx:Request request) {
        checkpanic caller->respond()
    }
}