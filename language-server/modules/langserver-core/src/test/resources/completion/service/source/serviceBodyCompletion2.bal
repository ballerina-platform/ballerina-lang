import ballerina/http;

listener http:Listener ep1 = new(9090);

@http:ServiceConfig {
    basePath: "/cbrBase"
}
service myService1 on ep1 {
    r
    resource function foo(http:Caller caller, http:Request req) {
        _ = caller->respond("Hello");
    }
}
