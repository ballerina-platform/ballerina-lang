import ballerina/net.http;

endpoint http:ServiceEndpoint echoEP {
    port:9099
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP{

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo (endpoint outboundEP, http:Request req) {
        http:Response resp = {};
        var result = req.getStringPayload();
        match result {
            string payload => {
                resp.setStringPayload(payload);
                _ = outboundEP -> respond(resp);
            }
            any | null => {
                return;
            }
        }
    }
}
