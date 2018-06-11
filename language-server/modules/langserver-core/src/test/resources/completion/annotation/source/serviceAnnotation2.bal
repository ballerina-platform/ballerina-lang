import ballerina/http;

@http:ServiceConfig {
    
}
@
service<http:Service> serviceName {
    newResource (endpoint caller, http:Request request) {
    }
}