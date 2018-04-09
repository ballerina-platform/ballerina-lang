import ballerina/http;

endpoint http:ServiceEndpoint chunkingEP {
    port:9092
};

endpoint http:ServiceEndpoint echoEP {
    port:9090
};

//Config client endpoint chunking behaviour by adding auto (default value), always or never to chunking option.
endpoint http:ClientEndpoint clientEndpoint {
    targets: [
        {
           uri: "http://localhost:9090"
        }
    ],
    chunking: http:Chunking.NEVER
};

@http:ServiceConfig {
}
service<http:Service> chunkingSample bind chunkingEP {

    @Description {value:"Server does a backend call using chunking disabled HttpClient"}
    @http:ResourceConfig {
        path:"/"
    }
    sample (endpoint conn, http:Request req) {
        //Create new outbound request and set payload.
        http:Request newReq = new;
        newReq.setJsonPayload({"hello":"world!"});
        var result = clientEndpoint -> post("/echo/", newReq);
        match result {
            http:Response clientResponse => {
                //Forward the inbound response.
                _ = conn -> forward(clientResponse);
            }
            http:HttpConnectorError err => {
                http:Response errorResponse = new;
                json errMsg = {"error":"error occurred while invoking the service"};
                errorResponse.setJsonPayload(errMsg);
                _ = conn -> respond(errorResponse);
            }
        }
    }
}

@Description {value:"Sample backend which respond according chunking behaviour."}
@http:ServiceConfig {
}
service<http:Service> echo bind echoEP {
    @http:ResourceConfig {
        path:"/"
    }
    echoResource (endpoint conn, http:Request req) {
        string value;
        //Set response according to the request headers.
        if (req.hasHeader("content-length")) {
            value = "Lenght-" + req.getHeader("content-length");
        } else if (req.hasHeader("Transfer-Encoding")) {
            value = req.getHeader("Transfer-Encoding");
        } else {
            value = "Neither Transfer-Encoding nor content-length header found";
        }
        http:Response res = new;
        res.setJsonPayload({"Outbound request content":value});
        _ = conn -> respond(res);
    }
}
