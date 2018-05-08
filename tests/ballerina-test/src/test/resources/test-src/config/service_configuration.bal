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
    sayHello (endpoint caller, http:Request request) {
        http:Response response = new;
        response.setTextPayload("Hello World!!!");
        _ = caller -> respond(response);
    }
}
