import ballerina/http;

@
service<http:Service> helloService {
    helloResource (endpoint caller, http:Request request) {
    }
}