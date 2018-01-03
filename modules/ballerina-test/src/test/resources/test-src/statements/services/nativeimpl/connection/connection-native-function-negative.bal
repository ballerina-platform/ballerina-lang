import ballerina.net.http;

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/10"
    }
    resource echo1 (http:Connection conn, http:Request req) {
        http:Response resp = {};
        _ = conn.respond(null);
    }

    @http:resourceConfig {
        path:"/11"
    }
    resource echo3 (http:Connection conn, http:Request req) {
        http:Response resp = {};
        http:Connection connn = {};
        _ = connn.respond(resp);
    }
}
