import ballerina/log;
import ballerina/io;
import ballerina/http;

@http:ServiceConfig {
    basePath: "/continue"
}
service continueBackend1 on new http:Listener(9243) {
    @http:ResourceConfig {
        path: "/"
    }
    resource function hello(http:Caller caller, http:Request request) {
        if (request.expects100Continue()) {
            string mediaType = request.getHeader("Content-Type");
            if (mediaType.toLowerAscii() == "text/plain") {
                var result = caller->continue();
                if (result is error) {
                    log:printError("Error sending response", err = result);
                }
            } else {
                http:Response res = new;
                res.statusCode = 417;
                res.setPayload("Unprocessable Entity");
                var result = caller->respond(res);
                if (result is error) {
                    log:printError("Error sending response", err = result);
                }
                return;
            }
        }
        http:Response res = new;
        var payload = request.getTextPayload();
        log:printInfo(<string>payload);
        if (payload is string) {
            res.statusCode = 200;
            res.setPayload(<@untainted> payload);
            var result = caller->respond(res);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }
        } else {
            res.statusCode = 500;
            res.setPayload(<@untainted> <string> payload.detail()?.message);
            var result = caller->respond(res);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }
        }
    }
}

public function main() {
    http:Client clientEP = new("http://localhost:9243");
    http:Request req = new();
    req.addHeader("content-type", "text/plain");
    req.addHeader("Expect", "100-continue");
    req.setPayload("Hello World!");
    var response = clientEP->post("/continue", req);
    if (response is http:Response) {
        var payload = response.getTextPayload();
        if (payload is string) {
            io:print(payload);
            io:print(response.statusCode);
        } else {
            io:println(<string>payload.detail()["message"]);
        }
    } else {
        io:println(<string>response.detail()["message"]);
    }
}
