import ballerina/http;

listener http:Listener ep1 = new(9090);

@http:ServiceConfig {
    basePath: "/cbrBase"
}
service myService1 on ep1 {
    string testString = "Hello";
    string testString2 =
    resource function foo(http:Caller caller, http:Request req) {
        _ = caller->respond("Hello");
    }
}
