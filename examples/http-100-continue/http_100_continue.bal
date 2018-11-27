import ballerina/http;
import ballerina/log;

@http:ServiceConfig {
    basePath: "/hello"
}
service<http:Service> helloWorld bind { port: 9090 } {

    @http:ResourceConfig {
        path: "/"
    }
    hello(endpoint caller, http:Request request) {
        // Check if the client expects a 100-continue response.
        if (request.expects100Continue()) {
            // Send a 100-continue response to the client.
            var result = caller->respond(res);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }
        }

        // The client starts sending the payload once it receives the 100-continue response.
        // Retrieve the payload that is sent by the client.
        http:Response res = new;
        match request.getTextPayload() {
            string payload => {
                log:printInfo(payload);
                res.statusCode = 200;
                res.setPayload("Hello World!\n");
                var result = caller->respond(res);
                if (result is error) {
                    log:printError("Error sending response", err = result);
                }
            }
            error err => {
                res.statusCode = 500;
                res.setPayload(untaint err.message);

                var result = caller->respond(res);
                if (result is error) {
                    log:printError("Error sending response", err = result);
                }
            }
        }
    }
}
