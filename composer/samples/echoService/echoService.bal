import ballerina/http;

@http:configuration {basePath:"/echo"}
service<http> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:Connection conn, http:Request req) {
        http:Response resp = {};
        var payload, payloadError = req.getTextPayload();
        if (payloadError == null) {
            resp.setTextPayload(payload);
        } else {
            resp.statusCode = 500;
            resp.setTextPayload(payloadError.message);
        }

        _ = conn.respond(resp);
    }
}
