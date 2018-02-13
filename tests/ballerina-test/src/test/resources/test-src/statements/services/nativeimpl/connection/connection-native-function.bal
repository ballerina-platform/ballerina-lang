import ballerina.net.http;

@http:configuration {
    basePath:"/hello"
}
service<http> helloServer {
    @http:resourceConfig {
        path:"/redirect",
        methods:["GET"]
    }
    resource redirect(http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        _ = conn.redirect(res, http:redirectCode.MOVED_PERMANENTLY_301, ["location1"]);
    }
}
