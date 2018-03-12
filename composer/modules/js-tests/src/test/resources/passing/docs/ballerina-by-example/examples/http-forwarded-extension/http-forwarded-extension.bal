import ballerina.net.http;

service<http> proxy {

    @Description {value:"Proxy server forward the inbound request to a backend with forwarded config enabled."}
    @http:resourceConfig {
        path:"/"
    }
    resource sample (http:Connection conn, http:InRequest req) {
        //Configure client connector forwarded/x-forwarded-- header behaviour by adding disable (default value), enable or transition.
        //Transition config converts available x-forwarded-- headers to forwarded header.
        endpoint<http:HttpClient> endPoint {
            create http:HttpClient("http://localhost:9090/sample", {forwarded:"enable"});
        }
        var clientResponse, _ = endPoint.forward("/", req);
        _ = conn.forward(clientResponse);
    }
}

@Description {value:"Sample backend which respond with forwarded header value."}
service<http> sample {
    @http:resourceConfig {
        path:"/"
    }
    resource sampleResource (http:Connection conn, http:InRequest req) {
        string value = req.getHeader("forwarded");
        http:OutResponse res = {};
        res.setStringPayload("forwarded header value : " + value);
        _ = conn.respond(res);
    }
}
