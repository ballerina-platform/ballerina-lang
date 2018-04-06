import ballerina/http;
import ballerina/io;

endpoint http:ServiceEndpoint helloWorldEP {
    port:9090
};

endpoint http:ClientEndpoint clientEP {
    targets:[{url: "http://httpstat.us"}]
};

@http:ServiceConfig { basePath:"/hello", endpoints:[helloWorldEP] }
service<http:Service> helloWorld bind helloWorldEP{
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint conn, http:Request req) {
        var resp = clientEP -> forward("/200", req);
        match resp {
            http:HttpConnectorError err => io:println(err.message);
            http:Response response => _ = conn -> forward(response);
        }
    }
}