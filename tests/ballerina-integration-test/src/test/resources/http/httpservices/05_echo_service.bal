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
        http:Response resp = new;
        match payload {
            string payloadValue => {
                resp.setTextPayload(untaint payloadValue);
                _ = caller -> respond(resp);
            }
            error payloadErr => {
                resp.statusCode = 500;
                string errMsg = <string> payloadErr.detail().message;
                resp.setPayload(errMsg);
                log:printError("Failed to retrieve payload from request: " + payloadErr.reason());
                caller->respond(resp) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}
