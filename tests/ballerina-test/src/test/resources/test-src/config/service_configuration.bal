import ballerina/http;
import ballerina/config;

endpoint http:Listener backendEP {
    port: config:getAsInt("backendEP.port")
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
        http:Response response = new;
        response.setStringPayload("Hello World!!!");
        _ = outboundEP -> respond(response);
    }
}
