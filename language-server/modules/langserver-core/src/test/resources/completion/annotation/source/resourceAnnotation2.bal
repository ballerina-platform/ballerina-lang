import ballerina/http;

service<http:Service> helloService {
    @http:ResourceConfig {
        
    }
    @
    helloResource (endpoint caller, http:Request request) {
    }
}