import ballerina/http;
import ballerina/io;

endpoint http:Listener helloWorldEP {
    port:9090
};

endpoint http:Client clientEP {
    url: "http://httpstat.us"
};

@http:ServiceConfig { basePath:"/hello", endpoints:[helloWorldEP] }
service<http:Service> helloWorld bind helloWorldEP{
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint caller, http:Request req) {
        var resp = clientEP -> forward("/200", req);
        match resp {
            http:HttpConnectorError err => io:println(err.message);
            http:Response response => _ = caller -> respond(response);
        }
    }
}