import ballerina.net.http;

@http:configuration {basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Connection conn, http:Request req) {
        http:OutResponse resp = {};
        var payload, _ = req.getStringPayload();
        resp.setStringPayload(payload);
        _ = conn.respond(resp);
    }
}