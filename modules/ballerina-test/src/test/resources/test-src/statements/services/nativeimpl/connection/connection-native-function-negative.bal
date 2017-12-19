import ballerina.net.http;

@http:configuration{basePath:"/hello"}
service<http> helloServer {

    @http:resourceConfig {
        path:"/10"
    }
    resource echo1 (http:Connection con, http:Request req) {
        http:Response resp = {};
        _ = con.respond(null);
    }

    @http:resourceConfig {
        path:"/11"
    }
    resource echo2 (http:Connection con, http:Request req) {
        http:Response res = {};
        http:Response resp = {};
        res.setStringPayload("wso2");
        resp.setStringPayload("Ballerina");
        _ = con.respond(res);
        _ = con.respond(resp);
    }

    @http:resourceConfig {
        path:"/12"
    }
    resource echo3 (http:Connection con, http:Request req) {
        http:Response resp = {};
        http:Connection conn = {};
        _ = conn.respond(resp);
    }
}
