import ballerina/http;
import ballerina/io;

endpoint http:Listener echoEP {
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
    echo (endpoint caller, http:Request req) {
        var payload = req.getTextPayload();
        match payload {
            string payloadValue => {
                http:Response resp = new;
                resp.setTextPayload(payloadValue);
                _ = caller -> respond(resp);
            }
            any | () => {
                io:println("Error while fetching string payload");
            }
        }
    }
}
