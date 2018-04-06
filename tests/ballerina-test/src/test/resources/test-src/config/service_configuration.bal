import ballerina/http;
import ballerina/config;

endpoint http:ServiceEndpoint backendEP {
    port: getBackendPort()
};

@http:ServiceConfig {
    basePath: config:getAsString("hello.basePath")
}
service<http:Service> hello bind backendEP{

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint outboundEP, http:Request request) {
        http:Response response = {};
        response.setStringPayload("Hello World!!!");
        _ = outboundEP -> respond(response);
    }
}

function getBackendPort() (int) {
    var port, err = <int>config:getAsString("backendEP.port");
    return err == null ? port : 8000;
}
