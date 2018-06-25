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
        //Set the response according to the request headers.
        if (req.hasHeader("content-length")) {
            value = "Length-" + req.getHeader("content-length");
        } else if (req.hasHeader("Transfer-Encoding")) {
            value = req.getHeader("Transfer-Encoding");
        } else {
            value = "Neither Transfer-Encoding nor content-length header found";
        }
        http:Response res = new;
        res.setPayload({ "Outbound request content": value });
        caller->respond(res) but { error e => log:printError("Error sending response from echo service", err = e) };
    }
}
