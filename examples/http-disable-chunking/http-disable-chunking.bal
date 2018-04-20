import ballerina/http;

endpoint http:Listener chunkingEP {
    port:9092
};

endpoint http:Listener echoEP {
    port:9090
};

//The HTTP client's chunking behaviour can be configured as auto, always, or never. In this example, it is set to as never, which means that chunking
//never happens irrespective of how it is specified in the reqest. When chunking is set to auto, it is done as specified in the request.
endpoint http:Client clientEndpoint {
    url: "http://localhost:9090",
    chunking: http:CHUNKING_NEVER
};

@http:ServiceConfig {
}
service<http:Service> chunkingSample bind chunkingEP {

    @Description {value:"The server does a backend call using chunking disabled HttpClient"}
    @http:ResourceConfig {
        path:"/"
    }
    sample (endpoint caller, http:Request req) {
        //Create a new outbound request and set the payload.
        http:Request newReq = new;
        newReq.setJsonPayload({"hello":"world!"});
        var result = clientEndpoint -> post("/echo/", newReq);
        match result {
            http:Response clientResponse => {
                //Forward the inbound response.
                _ = caller -> respond(clientResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = caller -> respond(errorResponse);
            }
        }
    }
}

@Description {value:"A sample backend that responds according to chunking behaviour."}
@http:ServiceConfig {
}
service<http:Service> echo bind echoEP {
    @http:ResourceConfig {
        path:"/"
    }
    echoResource (endpoint caller, http:Request req) {
        string value;
        //Set the response according to the request headers.
        if (req.hasHeader("content-length")) {
            value = "Lenght-" + req.getHeader("content-length");
        } else if (req.hasHeader("Transfer-Encoding")) {
            value = req.getHeader("Transfer-Encoding");
        } else {
            value = "Neither Transfer-Encoding nor content-length header found";
        }
        http:Response res = new;
        res.setJsonPayload({"Outbound request content":value});
        _ = caller -> respond(res);
    }
}
