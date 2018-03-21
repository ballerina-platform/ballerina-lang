import ballerina.net.http;
import ballerina.log;

endpoint http:ServiceEndpoint echoEP {
    port:9099
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo bind echoEP {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo (endpoint outboundEP, http:Request req) {
        http:Response resp = {};
        var payload = req.getStringPayload();
        match payload {
            string s1 => {
                resp.setStringPayload(s1);
            }
            any|null => {
                resp.setStringPayload("payload not found");
            }
        }
        _ = outboundEP -> respond(resp);
    }
}