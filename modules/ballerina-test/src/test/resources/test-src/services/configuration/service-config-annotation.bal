import ballerina.net.http;

@http:configuration {basePath:"/hello"}
@http:configuration {port: 9090}
service<http> helloWorldServiceConfig {

    @http:resourceConfig {
        methods:["GET"],
        path:"/"
    }
    resource sayHello (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        res.setStringPayload("Hello World !!!");
        _ = conn.respond(res);
    }
}
