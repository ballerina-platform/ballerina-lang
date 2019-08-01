import ballerina/http;

listener http:MockListener mockEP = new(9090);

service myService1 on mockEP {
    resource function foo(http:Caller caller, http:Request req) {
        caller->respond()
    }
}