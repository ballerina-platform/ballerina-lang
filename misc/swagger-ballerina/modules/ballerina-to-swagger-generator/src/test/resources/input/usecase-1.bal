import ballerina/http;

endpoint http:Client backendClientEP {
    targets:[{url: "http://localhost:8080"}]
};

endpoint http:Listener backendEP {
    port:8080
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> Service1 bind backendEP {

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
