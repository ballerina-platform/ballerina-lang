import ballerina.net.http;

endpoint<http:Service> chunkingEP {
    port:9092
}

//Config client connector chunking behaviour by adding auto (default value), always or never to chunking option.
endpoint<http:Client> clientEndpoint {
    serviceUri: "http://localhost:9090",
    chunking: http:Chunking.NEVER
}

@http:serviceConfig { endpoints:[chunkingEP] }
service<http:Service> chunkingSample {

    @Description {value:"Server does a backend call using chunking disabled HttpClient"}
    @http:resourceConfig {
        path:"/"
    }
    resource sample (http:ServerConnector conn, http:Request req) {
        //Create new outbound request and set payload.
        http:Request newReq = {};
        newReq.setJsonPayload({"hello":"world!"});
        var clientResponse, _ = clientEndpoint -> post("/echo/", newReq);
        //Forward the inbound response.
        _ = conn -> forward(clientResponse);
    }
}

@Description {value:"Sample backend which respond according chunking behaviour."}
service<http:Service> echo {
    @http:resourceConfig {
        path:"/"
    }
    resource echoResource (http:ServerConnector conn, http:Request req) {
        string value;
        //Set response according to the request headers.
        if (req.getHeader("content-length") != null) {
            value = "Lenght-" + req.getHeader("content-length");
        } else {
            value = req.getHeader("Transfer-Encoding");
        }
        http:Response res = {};
        res.setJsonPayload({"Outbound request content":value});
        _ = conn -> respond(res);
    }
}
