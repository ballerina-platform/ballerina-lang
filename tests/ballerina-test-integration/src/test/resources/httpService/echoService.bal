import ballerina.net.http;

endpoint<http:Service> echoEp {
    port:9099
}

@http:serviceConfig {
    basePath:"/echo",
    endpoints:[echoEp]
}
service<http:Service> echo {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    resource echo (http:ServerConnector conn, http:Request req) {
        http:Response resp = {};
        var payload, _ = req.getStringPayload();
        resp.setStringPayload(payload);
        _ = conn -> respond(resp);
    }
}