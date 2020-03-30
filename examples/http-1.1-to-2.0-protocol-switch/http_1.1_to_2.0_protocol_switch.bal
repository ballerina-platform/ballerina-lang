import ballerina/http;
import ballerina/log;

// HTTP version is set to 2.0.
http:Client http2serviceClientEP = new ("http://localhost:7090", {httpVersion: "2.0"});

@http:ServiceConfig {
    basePath: "/http11Service"
}
service http11Service on new http:Listener(9090) {

    @http:ResourceConfig {
        path: "/"
    }
    resource function http11Resource(http:Caller caller,
                                     http:Request clientRequest) {
        // Forward the `clientRequest` to the `http2` service.
        var clientResponse = http2serviceClientEP->forward("/http2service",
                                                        clientRequest);

        http:Response response = new;
        if (clientResponse is http:Response) {
            response = clientResponse;
        } else {
            // Handle the errors that are returned when invoking the
            // `forward` function.
            response.statusCode = 500;
            response.setPayload(<string>clientResponse.detail()?.message);
        }
        // Send the response back to the caller.
        var result = caller->respond(response);
        if (result is error) {
           log:printError("Error occurred while sending the response",
               err = result);
        }

    }
}

// HTTP version is set to 2.0.
listener http:Listener http2serviceEP = new (7090,
    config = {httpVersion: "2.0"});

@http:ServiceConfig {
    basePath: "/http2service"
}
service http2service on http2serviceEP {

    @http:ResourceConfig {
        path: "/"
    }
    resource function http2Resource(http:Caller caller,
                                    http:Request clientRequest) {
        // Construct the response message.
        http:Response response = new;
        json msg = {"response": {"message": "response from http2 service"}};
        response.setPayload(msg);

        // Send the response back to the caller (http11Service).
        var result = caller->respond(response);
        if (result is error) {
            log:printError("Error occurred while sending the response",
                err = result);
        }
    }
}
