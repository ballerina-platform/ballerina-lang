import ballerina/net.http;

endpoint http:ServiceEndpoint helloWorldEp {
    port:9090
};

@http:ServiceConfig {
    basePath:"/hello",
    endpoints:[helloWorldEp]
}
service<http:Service> helloWorld {

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

