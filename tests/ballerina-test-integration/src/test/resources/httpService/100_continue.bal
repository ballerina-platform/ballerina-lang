import ballerina/http;
import ballerina/log;

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
                res.setPayload(payload);
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }

            error err => {
                res.statusCode = 500;
                res.setPayload(err.message);
                log:printError("Failed to retrieve payload from request: " + err.message);
                caller->respond(res) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
        }
    }
}
