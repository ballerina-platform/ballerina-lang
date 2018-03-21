import ballerina.net.http;

endpoint http:ServiceEndpoint echoEP {
    port:9099
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo {

    @http:ResourceConfig {
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