import ballerina/http;

@http:ServiceConfig {
    basePath: "/testPath"
}
service helloService on new http:Listener(8080) {
    resource function helloResource(http:Caller caller, http:Request request) {
    }
}
