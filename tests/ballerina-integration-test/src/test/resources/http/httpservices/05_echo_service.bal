import ballerina/http;
import ballerina/io;

endpoint http:Listener echoEP1 {
    port:9094
};

@http:ServiceConfig {
    basePath:"/echo"
}
service<http:Service> echo1 bind echoEP1 {

    @http:ResourceConfig {
        methods:["POST"],
        path:"/"
    }
    echo1 (endpoint caller, http:Request req) {
        var payload = req.getTextPayload();
        match payload {
            string payloadValue => {
                http:Response resp = new;
                resp.setTextPayload(untaint payloadValue);
                _ = caller -> respond(resp);
            }
            any | () => {
                io:println("Error while fetching string payload");
            }
        }
    }
}
