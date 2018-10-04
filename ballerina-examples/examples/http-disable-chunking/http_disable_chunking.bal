import ballerina/http;
import ballerina/log;

//The HTTP client's chunking behaviour can be configured as auto, always, or never.
//In this example, it is set to as never, which means that chunking never happens irrespective of how it is specified
//in the request. When chunking is set to auto, chunking is done as specified in the request.
endpoint http:Client clientEndpoint {
    url: "http://localhost:9090",
    chunking: http:CHUNKING_NEVER
};

service<http:Service> chunkingSample bind { port: 9092 } {

    @http:ResourceConfig {
        path: "/"
    }
    //Parameters include a reference to the caller endpoint and an object with the request data.
    invokeEndpoint(endpoint caller, http:Request req) {
        //Create a new outbound request and set the payload.
        http:Request newReq = new;
        newReq.setPayload({ "name": "Ballerina" });
        var result = clientEndpoint->post("/echo/", newReq);
        match result {
            http:Response clientResponse => {
                //send the response back to the caller.
                caller->respond(clientResponse) but { error e => log:printError("Error sending response", err = e) };
            }
            error responseError => {
                http:Response errorResponse = new;
                json errMsg = { "error": "error occurred while invoking the service" };
                errorResponse.setPayload(errMsg);
                caller->respond(errorResponse) but { error e => log:printError("Error sending response", err = e) };
            }
        }
    }
}

// A sample backend that responds according to chunking behaviour.
service<http:Service> echo bind { port: 9090 } {
    @http:ResourceConfig {
        path: "/"
    }
    echoResource(endpoint caller, http:Request req) {
        string value;

        http:Response res = new;
        boolean validationErrorFound = false;
        //Set the response according to the request headers.
        if (req.hasHeader("content-length")) {
            value = req.getHeader("content-length");
            //Perform data validation for content-length.
            if (check value.matches("\\d+")) {
                value = "Length-" + value;
            } else {
                res.statusCode = 400;
                res.setPayload("Content-Length contains invalid data");
                validationErrorFound = true;
            }
        } else if (req.hasHeader("Transfer-Encoding")) {
            value = req.getHeader("Transfer-Encoding");
            //Perform data validation for transfer-encoding.
            if (value != "chunked" && value != "compress" && value != "deflate"
                && value != "gzip" && value != "identity") {
                res.statusCode = 400;
                res.setPayload("Transfer-Encoding contains invalid data");
                validationErrorFound = true;
            }
        } else {
            value = "Neither Transfer-Encoding nor content-length header found";
        }

        if (!validationErrorFound) {
            // Since there is no validation error, mark the `value` as trusted data and set to the response.
            res.setPayload({ "Outbound request content": untaint value });
        }
        caller->respond(res) but { error e => log:printError("Error sending response from echo service", err = e) };
    }
}
