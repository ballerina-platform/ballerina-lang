import ballerina.net.http;

@http:configuration {
    basePath:"/hello"
}
service<http> helloServer {
    @http:resourceConfig {
        path:"/redirect",
        methods:["GET"]
    }
    resource redirect(http:Connection conn, http:Request req) {
        http:Response res = {};
        _ = conn.redirect(res, http:RedirectCode.MOVED_PERMANENTLY_301, ["location1"]);
    }
}
