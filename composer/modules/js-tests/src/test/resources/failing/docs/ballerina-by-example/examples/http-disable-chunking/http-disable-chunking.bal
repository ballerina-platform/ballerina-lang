import ballerina.net.http;

service<http> chunkingSample {

    @Description {value:"Server does a backend call using chunking disabled HttpClient"}
    @http:resourceConfig {
        path:"/"
    }
    resource sample (http:Connection conn, http:InRequest req) {
        //Config client connector chunking behaviour by adding auto (default value), always or never to chunking option.
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090", {chunking:"never"});
        }
        //Create new outbound request and set payload.
        http:OutRequest newReq = {};
        newReq.setJsonPayload({"hello":"world!"});
        var clientResponse, _ = endPoint.post("/echo/", newReq);
        //Forward the inbound response.
        _ = conn.forward(clientResponse);
    }
}

@Description {value:"Sample backend which respond according chunking behaviour."}
service<http> echo {
    @http:resourceConfig {
        path:"/"
    }
    resource echoResource (http:Connection conn, http:InRequest req) {
        string value;
        //Set response according to the request headers.
        if (req.getHeader("content-length") != null) {
            value = "Lenght-" + req.getHeader("content-length");
        } else {
            value = req.getHeader("Transfer-Encoding");
        }
        http:OutResponse res = {};
        res.setJsonPayload({"Outbound request content":value});
        _ = conn.respond(res);
    }
}
