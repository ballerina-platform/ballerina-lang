import ballerina.net.http;

endpoint<http:Service> serverEP {
    port:9090
}

//Configure client connector forwarded/x-forwarded-- header behaviour by adding disable (default value), enable or transition.
//Transition config converts available x-forwarded-- headers to forwarded header.
endpoint<http:Client> clientEndPoint {
    serviceUri: "http://localhost:9090/sample",
    forwarded:"enable"
}

@http:serviceConfig { endpoints:[serverEP] }
service<http:Service> proxy {

    @Description {value:"Proxy server forward the inbound request to a backend with forwarded config enabled."}
    @http:resourceConfig {
        path:"/"
    }
    resource sample (http:ServerConnector conn, http:Request req) {
        var clientResponse, _ = clientEndPoint -> forward("/", req);
        _ = conn -> forward(clientResponse);
    }
}

@Description {value:"Sample backend which respond with forwarded header value."}
@http:serviceConfig { endpoints:[serverEP] }
service<http:Service> sample {
    @http:resourceConfig {
        path:"/"
    }
    resource sampleResource (http:ServerConnector conn, http:Request req) {
        string value = req.getHeader("forwarded");
        http:Response res = {};
        res.setStringPayload("forwarded header value : " + value);
        _ = conn -> respond(res);
    }
}
