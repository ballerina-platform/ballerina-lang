import ballerina/http;
import ballerina/log;

endpoint http:Client http2serviceClientEP {
    url: "http://localhost:7090",
    // HTTP version is set to 2.0.
    httpVersion: "2.0"

};

@http:ServiceConfig {
    basePath: "/http11Service"
}
service<http:Service> http11Service bind { port: 9090 } {

    @http:ResourceConfig {
        path: "/"
    }
    http11Resource(endpoint caller, http:Request clientRequest) {
        // Forward the `clientRequest` to the `http2` service.
        var clientResponse =
            http2serviceClientEP->forward("/http2service", clientRequest);

        http:Response response = new;
        match clientResponse {
            http:Response resultantResponse => {
                response = resultantResponse;
            }
            error err => {
                // Handle the errors that are returned when invoking the `forward` function.
                response.statusCode = 500;
                response.setPayload(err.message);

            }
        }
        // Send the response back to the caller.
        caller->respond(response) but {
            error e => log:printError(
                           "Error occurred while sending the response",
                           err = e) };

    }
}

endpoint http:Listener http2serviceEP {
    port: 7090,
    // HTTP version is set to 2.0.
    httpVersion: "2.0"

};

@http:ServiceConfig {
    basePath: "/http2service"
}
service http2service bind http2serviceEP {

    @http:ResourceConfig {
        path: "/"
    }
    http2Resource(endpoint caller, http:Request clientRequest) {
        // Construct the response message.
        http:Response response = new;
        json msg = { "response": { "message": "response from http2 service" } };
        response.setPayload(msg);

        // Send the response back to the caller (http11Service).
        caller->respond(response) but {
            error e => log:printError(
                           "Error occurred while sending the response",
                           err = e) };

    }
}
