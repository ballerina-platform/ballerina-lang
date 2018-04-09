import ballerina/http;

endpoint http:ServiceEndpoint helloServiceEP {
    port:9095
};

@http:ServiceConfig {
    basePath: "/hello"
}
service<http:Service> helloService bind helloServiceEP {
    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    hello (endpoint outboundEP, http:Request request) {
        http:Response response = new;
        response.setStringPayload("Successful");
        _ = outboundEP -> respond(response);
    }
}
