import ballerina/http;
import ballerina/mime;

@final string ACCEPT_ENCODING = "accept-encoding";

endpoint http:Listener passthroughEP {
    port:9090
};

endpoint http:Client acceptEncodingAutoEP {
    url: "http://localhost:9090/hello",
    acceptEncoding:http:ACCEPT_ENCODING_AUTO
};

endpoint http:Client acceptEncodingEnableEP {
    url: "http://localhost:9090/hello",
    acceptEncoding:http:ACCEPT_ENCODING_ALWAYS
};

endpoint http:Client acceptEncodingDisableEP {
    url: "http://localhost:9090/hello",
    acceptEncoding:http:ACCEPT_ENCODING_NEVER
};

service<http:Service> passthrough bind passthroughEP {
    @http:ResourceConfig {
        path:"/"
    }
    passthrough (endpoint caller, http:Request req) {
        if (req.getHeader("AcceptValue") == "auto") {
            var clientResponse = acceptEncodingAutoEP -> post("/", request = req);
            match clientResponse {
                http:Response res => {
                    _ = caller -> respond(res);
                }
                http:HttpConnectorError err => {
                    http:Response res = new;
                    res.statusCode = 500;
                    res.setTextPayload(err.message);
                    _ = caller -> respond(res);
                }
            }
        } else if (req.getHeader("AcceptValue") == "enable") {
            var clientResponse = acceptEncodingEnableEP -> post("/", request = req);
            match clientResponse {
                http:Response res => {
                    _ = caller -> respond(res);
                }
                http:HttpConnectorError err => {
                    http:Response res = new;
                    res.statusCode = 500;
                    res.setTextPayload(err.message);
                    _ = caller -> respond(res);
                }
            }
        } else if (req.getHeader("AcceptValue") == "disable") {
            var clientResponse = acceptEncodingDisableEP -> post("/", request = req);
            match clientResponse {
                http:Response res => {
                    _ = caller -> respond(res);
                }
                http:HttpConnectorError err => {
                    http:Response res = new;
                    res.statusCode = 500;
                    res.setTextPayload(err.message);
                    _ = caller -> respond(res);
                }
            }
        }
    }
}

@Description {value:"Sample hello world service."}
service<http:Service> hello bind passthroughEP {

    @Description {value:"The helloResource only accepts requests made using the specified HTTP methods"}
    @http:ResourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    helloResource (endpoint caller, http:Request req) {
        http:Response res = new;
        json payload = {};
        boolean hasHeader = req.hasHeader(ACCEPT_ENCODING);
        if (hasHeader) {
            payload = {acceptEncoding:req.getHeader(ACCEPT_ENCODING)};
        } else {
            payload = {acceptEncoding:"Accept-Encoding hdeaer not present."};
        }
        res.setJsonPayload(payload);
        _ = caller -> respond(res);
    }
}
