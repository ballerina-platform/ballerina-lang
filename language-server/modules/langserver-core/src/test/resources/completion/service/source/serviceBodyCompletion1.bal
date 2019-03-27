import ballerina/http;

listener http:Listener ep1 = new(9090);

@http:ServiceConfig {
    basePath: "/cbrBase"
}
service myService1 on ep1 {
    
    resource function foo(http:Caller caller, http:Request req) {
        checkpanic caller->respond("Hello");
    }
}
