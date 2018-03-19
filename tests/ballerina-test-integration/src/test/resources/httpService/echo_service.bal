import ballerina.net.http;

endpoint http:ServiceEndpoint echoEP {
    port:9090
};

@http:serviceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP {

    @http:resourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo (endpoint outboundEP, http:Request req) {
        http:Response resp = {};
        var payload, _ = req.getStringPayload();
        resp.setStringPayload(payload);
        _ = outboundEP -> respond(resp);
    }
}
