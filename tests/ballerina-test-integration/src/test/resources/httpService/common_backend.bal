import ballerina/http;
import ballerina/io;

endpoint http:Listener echoEP {
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
        http:Response resp = new;
        var result = req.getStringPayload();
        match result {
            http:PayloadError payloadError => io:println(payloadError.message);
            string payload => {
                resp.setStringPayload(payload);
                _ = outboundEP -> respond(resp);
            }
        }
    }
}
