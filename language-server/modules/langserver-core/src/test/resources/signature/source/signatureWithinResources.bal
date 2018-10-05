import ballerina/http;

service<http:Service> greetService bind { port: 9090 } {
    sayHello (endpoint caller, http:Request request) {
        caller->respond(
    }
}