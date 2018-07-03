import ballerina/http;

endpoint http:Listener cbrEP {
    port: 9090
};

endpoint http:Client locationEp {
    url: "www.mock.io"
};

@http:ServiceConfig {
    basePath: "/cbrBase"
}
service<http:Service> serviceName bind  {

    newResource (endpoint caller, http:Request request) {
        
    }
}