import ballerina/http;
import ballerina/log;
import ballerina/mime;

http:Client clientEndpoint = new("http://localhost:9224");

@http:ServiceConfig {
    basePath: "/continue"
}
service helloContinue on new http:Listener(9090) {
    @http:ResourceConfig {
        path: "/"
    }
    resource function hello(http:Caller caller, http:Request request) {
        if (request.expects100Continue()) {
            if (request.hasHeader("X-Status")) {
                log:printInfo("Sending 100-Continue response");
                var responseError = caller->continue();
                if (responseError is error) {
                    log:printError("Error sending response", err = responseError);
                }
            } else {
                log:printInfo("Ignore payload by sending 417 response");
                http:Response res = new;
                res.statusCode = 417;
                res.setPayload("Do not send me any payload");
                var responseError = caller->respond(res);
                if (responseError is error) {
                    log:printError("Error sending response", err = responseError);
                }
                return;
            }
        }

        http:Response res = new;
        var result  = request.getTextPayload();

        if (result is string) {
            var responseError = caller->respond(untaint result);
            if (responseError is error) {
                log:printError("Error sending response", err = responseError);
            }
        } else {
            res.statusCode = 500;
            res.setPayload(untaint result.reason());
            log:printError("Failed to retrieve payload from request: " + result.reason());
            var responseError = caller->respond(res);
            if (responseError is error) {
                log:printError("Error sending response", err = responseError);
            }
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    resource function getFormParam(http:Caller caller, http:Request req) {
        string replyMsg = "Result =";
        var bodyParts = req.getBodyParts();
        if (bodyParts is mime:Entity[]) {
            int i = 0;
            while (i < bodyParts.length()) {
                mime:Entity part = bodyParts[i];
                mime:ContentDisposition contentDisposition = part.getContentDisposition();
                var result = part.getBodyAsString();
                if (result is string) {
                    replyMsg += " Key:" + contentDisposition.name + " Value: " + result;
                } else {
                    replyMsg += " Key:" + contentDisposition.name + " Value: " + result.reason();
                }
                i += 1;
            }
            var responseError = caller->respond(untaint replyMsg);
            if (responseError is error) {
                log:printError(responseError.reason(), err = responseError);
            }
        } else {
            log:printError(bodyParts.reason(), err = bodyParts);
        }
    }

    resource function testPassthrough(http:Caller caller, http:Request req) {
        if (req.expects100Continue()) {
            req.removeHeader("Expect");
            var responseError = caller->continue();
            if (responseError is error) {
                log:printError("Error sending response", err = responseError);
            }
        }
        var res = clientEndpoint->forward("/backend/hello", untaint req);
        if (res is http:Response) {
            var responseError = caller->respond(res);
            if (responseError is error) {
                log:printError("Error sending response", err = responseError);
            }
        } else {
            log:printError(res.reason(), err = res);
        }
    }
}

service backend on new http:Listener(9224) {
    resource function hello(http:Caller caller, http:Request request) {
        http:Response response = new;
        var payload = request.getTextPayload();
        if (payload is string) {
            response.setTextPayload(untaint payload);
        } else {
            response.setTextPayload(untaint payload.reason());
        }
        var responseError = caller->respond(response);
        if (responseError is error) {
            log:printError("Error sending response", err = responseError);
        }
    }
}
