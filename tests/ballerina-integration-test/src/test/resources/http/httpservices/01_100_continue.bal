import ballerina/http;
import ballerina/log;
import ballerina/mime;

endpoint http:Client clientEndpoint {
    url: "http://localhost:9224"
};

@http:ServiceConfig {
    basePath: "/continue"
}
service<http:Service> helloContinue bind { port: 9090 } {
    @http:ResourceConfig {
        path: "/"
    }
    hello(endpoint caller, http:Request request) {
        if (request.expects100Continue()) {
            log:printInfo("Sending 100-Continue response");
            var responseError = caller-> continue();
            if (responseError is error) {
                log:printError("Error sending response", err = responseError);
            }
            log:printInfo("100-Continue response sent");
        }

        http:Response res = new;
        var result  = request.getTextPayload();

        if (result is string) {
            var responseError = caller->respond(untaint result);
            if (responseError is error) {
                log:printError("Error sending response", err = responseError);
            }
        } else if (result is error) {
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
    getFormParam(endpoint caller, http:Request req) {
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
                } else if (result is error) {
                    replyMsg += " Key:" + contentDisposition.name + " Value: " + result.reason();
                }
                i += 1;
            }
            var responseError = caller->respond(untaint replyMsg);
            if (responseError is error) {
                log:printError(responseError.reason(), err = responseError);
            }
        } else if (bodyParts is error) {
            log:printError(bodyParts.reason(), err = bodyParts);
        }
    }

    testPassthrough (endpoint caller, http:Request req) {
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
        } else if (res is error) {
            log:printError(res.reason(), err = res);
        }
    }
}

service<http:Service> backend bind { port: 9224 } {
    hello (endpoint caller, http:Request request) {
        http:Response response = new;
        var payload = request.getTextPayload();
        if (payload is string) {
            response.setTextPayload(untaint payload);
        } else if (payload is error) {
            response.setTextPayload(untaint payload.reason());
        }
        var responseError = caller->respond(response);
        if (responseError is error) {
            log:printError("Error sending response", err = responseError);
        }
    }
}
