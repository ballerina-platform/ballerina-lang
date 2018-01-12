import ballerina.net.http;

service<http> chunkingSample {

    @Description {value:"Server does a backend call using chunking disabled HttpClient"}
    @http:resourceConfig {
        path:"/"
    }
    resource sample (http:Request req, http:Response res) {
        //Config client connector chunking behaviour by adding auto (default value), always or never to chunking option.
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090", {chunking:"never"});
        }
        //Create new request and set payload.
        http:Request newReq = {};
        newReq.setJsonPayload({"hello":"world!"});
        var clientResponse, _ = endPoint.post("/echo/", newReq);
        //Respond the inbound response.
        _ = res.forward(clientResponse);
    }
}

@Description {value:"Sample backend which respond according chunking behaviour."}
service<http> echo {
    @http:resourceConfig {
        path:"/"
    }
    resource echoResource (http:Request req, http:Response res) {
        string value;
        //Set response according to the request headers.
        if (req.getHeader("Content-Length") != null) {
            value = "Lenght-" + req.getHeader("Content-Length").value;
        } else {
            value = req.getHeader("Transfer-Encoding").value;
        }
        res.setJsonPayload({"Outbound request content":value});
        _ = res.send();
    }
}
