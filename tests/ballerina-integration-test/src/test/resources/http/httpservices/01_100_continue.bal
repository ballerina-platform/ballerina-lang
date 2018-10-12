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
            caller->continue() but {
                error e => log:printError("Error sending response", err = e)
            };
            log:printInfo("100-Continue response sent");
        }

        http:Response res = new;

        match request.getTextPayload() {
            string payload => {
                res.statusCode = 200;
                res.setPayload(untaint payload);
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }

            error err => {
                res.statusCode = 500;
                res.setPayload(untaint err.message);
                log:printError("Failed to retrieve payload from request: " + err.message);
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }

    @http:ResourceConfig {
        methods: ["POST"]
    }
    getFormParam(endpoint caller, http:Request req) {
        string replyMsg = "Result =";
        mime:Entity[] bodyParts = check req.getBodyParts();
        int i = 0;
        while (i < lengthof bodyParts) {
            mime:Entity part = bodyParts[i];
            mime:ContentDisposition contentDisposition = part.getContentDisposition();
            replyMsg += " Key:" + contentDisposition.name + " Value: " + check part.getBodyAsString();
            i++;
        }
        caller->respond(untaint replyMsg) but {
            error err => log:printError(err.message, err = err)
        };
    }

    testPassthrough (endpoint caller, http:Request req) {
        if (req.expects100Continue()) {
            req.removeHeader("Expect");
            caller->continue() but {
                error e => log:printError("Error sending response", err = e)
            };
        }
        http:Response res = check clientEndpoint->forward("/backend/hello", untaint req);
        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }
}

service<http:Service> backend bind { port: 9224 } {
    hello (endpoint caller, http:Request request) {
        http:Response response = new;
        string payload = check request.getTextPayload();
        response.setTextPayload(untaint payload);
        caller->respond(response) but {
            error e => log:printError("Error sending response", err = e)
        };
    }
}
