import ballerina/http;

@http:ServiceConfig {
    basePath: "/testPath"
}
service<http:Service> helloService bind { port: 9090 } {
    helloResource (endpoint caller, http:Request request) {
    }
}
