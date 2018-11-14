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
        if (payload is string) {
            _ = caller -> respond(untaint payload);
        } else if (payload is error) {
            resp.statusCode = 500;
            string errMsg = <string> payload.detail().message;
            resp.setPayload(errMsg);
            log:printError("Failed to retrieve payload from request: " + payload.reason());
            var responseError = caller->respond(resp);
            if (responseError is error) {
                log:printError("Error sending response", err = responseError);
            }
        }
    }
}
