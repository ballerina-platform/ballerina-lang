import ballerina/http;
import ballerina/log;

endpoint http:Listener http11ServiceEP {
    port:9090
};

endpoint http:Client http2serviceClientEP {
    url:"http://localhost:7090",
    // HTTP version is set to 2.0.
    httpVersion:"2.0"
};

@http:ServiceConfig {
    basePath:"/http11Service"
}
service<http:Service> http11Service bind http11ServiceEP {

    @http:ResourceConfig {
        path:"/"
    }
    httpResource(endpoint caller, http:Request clientRequest) {
        // Forward the clientRequest to http2 service.
        var clientResponse = http2serviceClientEP->forward("/http2service", clientRequest);
        http:Response response = new;
        match clientResponse {
            http:Response resultantResponse => {
                response = resultantResponse;
            }
            http:HttpConnectorError err => {
                // Handle if there is an error returned from the forward function invocation.
                response.statusCode = 500;
                response.setStringPayload(err.message);
            }
        }
        caller->respond(response) but {
            error e => log:printError("Error occurred while sending the response", err = e) };
    }
}

endpoint http:Listener http2serviceEP {
    port:7090,
    // HTTP version is set to 2.0.
    httpVersion:"2.0"
};

@http:ServiceConfig {
    basePath:"/http2service"
}
service<http:Service> http2service bind http2serviceEP {

    @http:ResourceConfig {
        path:"/"
    }
    http2Resource(endpoint caller, http:Request req) {
        // Construct response message.
        http:Response response = new;
        json msg = {"response":{"message":"response from http2 service"}};
        response.setJsonPayload(msg);

        // Send the response.
        caller->respond(response) but {
            error e => log:printError("Error occurred while sending the response", err = e)};
    }
}

