import ballerina.net.http;

@http:configuration {basePath:"/hello"}
service<http> helloWorldResourceConfig {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    @http:resourceConfig {
        methods:["POST"]
    }
    resource sayHello (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Hello World !!!");
        _ = conn.respond(res);
    }
}
