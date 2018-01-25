import ballerina.net.http;

@http:configuration {basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Connection conn, http:InRequest req) {
        http:OutResponse resp = {};
        string payload = req.getStringPayload();
        resp.setStringPayload(payload);
        _ = conn.respond(resp);
    }
}
