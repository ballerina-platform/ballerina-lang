import ballerina/net.http;

endpoint http:ServiceEndpoint helloWorldEp {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello"
}
service<http:Service> helloWorld bind helloWorldEp {

    @http:ResourceConfig {
        methods:["GET"],
        path:"/"
    }
    sayHello (endpoint client, http:Request req) {
        http:Response resp = {};
        resp.setStringPayload("Hello, World!");
        _ = client -> respond(resp);
    }
}

