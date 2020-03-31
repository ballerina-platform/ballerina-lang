import ballerina/http;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/hello"
}
service helloWorld on new http:Listener(9090) {

    @http:ResourceConfig {
        path: "/"
    }
    resource function hello(http:Caller caller, http:Request request) {
        // Check if the client expects a 100-continue response.
        if (request.expects100Continue()) {
            string mediaType = request.getHeader("Content-Type");
            if (mediaType.toLowerAscii() == "text/plain") {
                // Send a 100-continue response to the client.
                var result = caller->continue();
                if (result is error) {
                    log:printError("Error sending response", result);
                }
            } else {
                // Send a 417 response to ignore the payload since content type is mismatched
                // with the expected content type.
                http:Response res = new;
                res.statusCode = 417;
                res.setPayload("Unprocessable Entity");
                var result = caller->respond(res);
                if (result is error) {
                    log:printError("Error sending response", result);
                }
                return;
            }
        }

        // The client starts sending the payload once it receives the
        // 100-continue response. Retrieve the payload that is sent by the client.
        http:Response res = new;
        var payload = request.getTextPayload();
        if (payload is string) {
            log:printInfo(payload);
            res.statusCode = 200;
            res.setPayload("Hello World!\n");
            var result = caller->respond(res);
            if (result is error) {
                log:printError("Error sending response", result);
            }
        } else {
            res.statusCode = 500;
            res.setPayload(<@untainted string>payload.detail()?.message);
            var result = caller->respond(res);
            if (result is error) {
                log:printError("Error sending response", result);
            }
        }
    }
}
