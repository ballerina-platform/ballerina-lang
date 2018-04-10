import ballerina/http;
import ballerina/io;

endpoint http:Listener serverEP {
    port:9090
};

//Configure the client connector 'forwarded/x-forwarded-- header behaviour' by adding 'disable' (default value), 'enable' or 'transition'.
//Transition config converts available 'x-forwarded-- headers' to forwarded header.
endpoint http:Client clientEndPoint {
    targets: [
       {
            url: "http://localhost:9090"
       }
    ],
    forwarded:"enable"
};

@http:ServiceConfig {
    basePath: "/proxy"
}
service<http:Service> proxy bind serverEP {

    @Description {value:"The proxy server forwards the inbound request to a backend with forwarded config enabled."}
    @http:ResourceConfig {
        path:"/"
    }
    sample (endpoint conn, http:Request req) {
        var response = clientEndPoint -> forward("/sample", req);
        match response {
            http:Response clientResponse => {
                _ = conn -> forward(clientResponse);
            }
            http:HttpConnectorError err => {
                io:println("Error occurred while invoking the service");
            }
        }
    }
}

@Description {value:"The sample backend that responds with the forwarded header value."}
@http:ServiceConfig {
    basePath: "/sample"
}
service<http:Service> sample bind serverEP {

    @http:ResourceConfig {
        path:"/"
    }
    sampleResource (endpoint conn, http:Request req) {
        http:Response res = new;
        string|() header;
        header = req.getHeader("forwarded");
        match header {
            string headerVal => {
                res.setStringPayload("forwarded header value : " + headerVal);
            }
            any | () => {
                res.setStringPayload("forwarded header value not found");
            }
        }
         _ = conn -> respond(res);
    }
}
