import ballerina/httpx;

@httpx:ServiceConfig {
    basePath: "/testPath"
}
service helloService on new httpx:Listener(8080) {
    resource function helloResource(http:Caller caller, httpx:Request request) {
    }
}
