import ballerina/http;

service<http:Service> sampleService bind { port: 9090 } {
    

    sampleResource (endpoint caller, http:Request request) {
    }
}
