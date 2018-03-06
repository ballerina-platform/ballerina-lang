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
        _ = conn.redirect(res, http:RedirectCode.MOVED_PERMANENTLY_301, ["location1"]);
    }

    @http:resourceConfig {
        path:"/connection",
        methods:["GET"]
    }
    resource connection (http:Connection conn, http:InRequest req) {
        http:OutResponse res = {};
        json connectionJson = {host:conn.host, port:conn.port, remoteAddress:conn.remoteAddress};
        res.statusCode = 200;
        res.setJsonPayload(connectionJson);
        _ = conn.respond(res);
    }
}
