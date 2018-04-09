import ballerina/http;
import ballerina/io;

endpoint http:Listener serverEP {
    port:9090
};

//Configure client connector forwarded/x-forwarded-- header behaviour by adding disable (default value), enable or transition.
//Transition config converts available x-forwarded-- headers to forwarded header.
endpoint http:Client clientEndPoint {
    targets: [
       {
            uri: "http://localhost:9090"
       }
    ],
    forwarded:"enable"
};

@http:ServiceConfig {
    basePath: "/proxy"
}
service<http:Service> proxy bind serverEP {

    @Description {value:"Proxy server forward the inbound request to a backend with forwarded config enabled."}
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

@Description {value:"Sample backend which respond with forwarded header value."}
@http:ServiceConfig {
    basePath: "/sample"
}
service<http:Service> sample bind serverEP {

    @http:ResourceConfig {
        path:"/"
    }
    sampleResource (endpoint conn, http:Request req) {
        http:Response res = {};
        string|null header;
        header = req.getHeader("forwarded");
        match header {
            string headerVal => {
                res.setStringPayload("forwarded header value : " + headerVal);
            }
            any | null => {
                res.setStringPayload("forwarded header value not found");
            }
        }
         _ = conn -> respond(res);
    }
}
