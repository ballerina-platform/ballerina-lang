import ballerina/http;
import ballerina/io;

endpoint http:ServiceEndpoint echoEP {
    port:9090
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
        var payload = req.getStringPayload();
        match payload {
            string payloadValue => {
                http:Response resp = new;
                resp.setStringPayload(payloadValue);
                _ = outboundEP -> respond(resp);
            }
            any | () => {
                io:println("Error while fetching string payload");
            }
        }
    }
}
