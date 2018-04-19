import ballerina/http;
import ballerina/mime;

const string ACCEPT_ENCODING = "accept-encoding";

endpoint http:Listener passthroughEP {
    port:9090
};

endpoint http:Client acceptEncodingAutoEP {
    url: "http://localhost:9092/hello",
    acceptEncoding:"auto"
};

endpoint http:Client acceptEncodingEnableEP {
    url: "http://localhost:9092/hello",
    acceptEncoding:"enable"
};

endpoint http:Client acceptEncodingDisableEP {
    url: "http://localhost:9092/hello",
    acceptEncoding:"disable"
};

service<http:Service> passthrough bind passthroughEP {
    @http:ResourceConfig {
        path:"/"
    }
    passthrough (endpoint outboundEP, http:Request req) {
        if (req.getHeader("AcceptValue") == "auto") {
            var clientResponse = acceptEncodingAutoEP -> post("/", req);
            match clientResponse {
                http:Response res => {
                    _ = outboundEP -> respond(res);
                }
                http:HttpConnectorError err => {
                    http:Response res = new;
                    res.statusCode = 500;
                    res.setStringPayload(err.message);
                    _ = outboundEP -> respond(res);
                }
            }
        } else if (req.getHeader("AcceptValue") == "enable") {
            var clientResponse = acceptEncodingEnableEP -> post("/", req);
            match clientResponse {
                http:Response res => {
                    _ = outboundEP -> respond(res);
                }
                http:HttpConnectorError err => {
                    http:Response res = new;
                    res.statusCode = 500;
                    res.setStringPayload(err.message);
                    _ = outboundEP -> respond(res);
                }
            }
        } else if (req.getHeader("AcceptValue") == "disable") {
            var clientResponse = acceptEncodingDisableEP -> post("/", req);
            match clientResponse {
                http:Response res => {
                    _ = outboundEP -> respond(res);
                }
                http:HttpConnectorError err => {
                    http:Response res = new;
                    res.statusCode = 500;
                    res.setStringPayload(err.message);
                    _ = outboundEP -> respond(res);
                }
            }
        }
    }
}

endpoint http:Listener helloEP {
    port:9092
};

@Description {value:"Sample hello world service."}
service<http:Service> hello bind helloEP {

    @Description {value:"The helloResource only accepts requests made using the specified HTTP methods"}
    @http:ResourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    helloResource (endpoint outboundEP, http:Request req) {
        http:Response res = new;
        json payload = {};
        boolean hasHeader = req.hasHeader(ACCEPT_ENCODING);
        if (hasHeader) {
            payload = {acceptEncoding:req.getHeader(ACCEPT_ENCODING)};
        } else {
            payload = {acceptEncoding:"Accept-Encoding hdeaer not present."};
        }
        res.setJsonPayload(payload);
        _ = outboundEP -> respond(res);
    }
}
