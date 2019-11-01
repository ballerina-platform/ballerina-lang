import ballerina/http;
import ballerina/log;

//The HTTP client's chunking behaviour can be configured as `CHUNKING_AUTO`, `CHUNKING_ALWAYS`, or `CHUNKING_NEVER`.
//In this example, it is set to `CHUNKING_NEVER`, which means that chunking never happens irrespective of how it is specified
//in the request. When chunking is set to `CHUNKING_AUTO`, chunking is done as specified in the request.

http:Client clientEndpoint = new("http://localhost:9090",
                                 { http1Settings : { chunking: http:CHUNKING_NEVER }});

service chunkingSample on new http:Listener(9092) {

    @http:ResourceConfig {
        path: "/"
    }
    resource function invokeEndpoint(http:Caller caller, http:Request req) {
        //Create a new outbound request and set the payload.
        http:Request newReq = new;
        newReq.setPayload({ "name": "Ballerina" });
        var clientResponse = clientEndpoint->post("/echo/", newReq);
        if (clientResponse is http:Response) {
            var result = caller->respond(clientResponse);
            if (result is error) {
               log:printError("Error sending response", err = result);
            }
        } else {
            http:Response errorResponse = new;
            json msg = { "error": "An error occurred while invoking the service." };
            errorResponse.setPayload(msg);
            var response = caller->respond(errorResponse);
            if (response is error) {
               log:printError("Error sending response", err = response);
            }
        }
    }
}

// A sample backend, which responds according to the chunking behaviour.
service echo on new http:Listener(9090) {
    @http:ResourceConfig {
        path: "/"
    }
    resource function echoResource(http:Caller caller, http:Request req) {
        string value;

        http:Response res = new;
        boolean validationErrorFound = false;
        //Set the response according to the request headers.
        if (req.hasHeader("content-length")) {
            value = req.getHeader("content-length");
            value = "Length-" + value;
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
            value =
                "Neither Transfer-Encoding nor content-length header found";
        }

        if (!validationErrorFound) {
            // Since there is no validation error, mark the `value` as trusted data and set it to the response.
            res.setPayload({ "Outbound request content": <@untainted> value });
        }
        var result = caller->respond(res);
        if (result is error) {
           log:printError("Error sending response from echo service",
                        err = result);
        }
    }
}

function isValid(boolean|error value) returns boolean {
    if (value is boolean) {
        return value;
    }
    return false;
}
