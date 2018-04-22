import ballerina/http;

endpoint http:Listener listener {
    port:9090
};

service<http:Service> serviceName b {
    newResource (endpoint caller, http:Request request) {
    }
}