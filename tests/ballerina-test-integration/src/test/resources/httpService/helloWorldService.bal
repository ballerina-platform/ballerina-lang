import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorld {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection conn, http:InRequest req) {
        http:OutResponse resp = {};
        resp.setStringPayload("Hello, World!");
        _ = conn.respond(resp);
    }
}