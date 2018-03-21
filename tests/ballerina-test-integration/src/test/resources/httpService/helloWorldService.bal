import ballerina/net.http;

endpoint<http:Service> helloWorldEp {
    port:9090
}

@http:serviceConfig {
    basePath:"/hello",
    endpoints:[helloWorldEp]
}
service<http:Service> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:ServerConnector conn, http:Request req) {
        http:Response resp = {};
        resp.setStringPayload("Hello, World!");
        _ = conn -> respond(resp);
    }
}