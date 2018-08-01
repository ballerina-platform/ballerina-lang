import ballerina/http;
import ballerina/mime;

@final string ACCEPT_ENCODING = "accept-encoding";

endpoint http:Listener passthroughEP2 {
    port:9091
};

endpoint http:Client acceptEncodingAutoEP {
    url: "http://localhost:9091/hello",
    compression:http:COMPRESSION_AUTO
};

endpoint http:Client acceptEncodingEnableEP {
    url: "http://localhost:9091/hello",
    compression:http:COMPRESSION_ALWAYS
};

endpoint http:Client acceptEncodingDisableEP {
    url: "http://localhost:9091/hello",
    compression:http:COMPRESSION_NEVER
};

service<http:Service> passthrough bind passthroughEP2 {
    @http:ResourceConfig {
        path:"/"
    }
    passthrough (endpoint caller, http:Request req) {
        if (req.getHeader("AcceptValue") == "auto") {
            var clientResponse = acceptEncodingAutoEP -> post("/",untaint req);
            match clientResponse {
                http:Response res => {
                    _ = caller -> respond(res);
                }
                error err => {
                    http:Response res = new;
                    res.statusCode = 500;
                    res.setTextPayload(err.message);
                    _ = caller -> respond(res);
                }
            }
        } else if (req.getHeader("AcceptValue") == "enable") {
            var clientResponse = acceptEncodingEnableEP -> post("/",untaint req);
            match clientResponse {
                http:Response res => {
                    _ = caller -> respond(res);
                }
                error err => {
                    http:Response res = new;
                    res.statusCode = 500;
                    res.setTextPayload(err.message);
                    _ = caller -> respond(res);
                }
            }
        } else if (req.getHeader("AcceptValue") == "disable") {
            var clientResponse = acceptEncodingDisableEP -> post("/",untaint req);
            match clientResponse {
                http:Response res => {
                    _ = caller -> respond(res);
                }
                error err => {
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
service<http:Service> hello bind passthroughEP2 {

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
        res.setJsonPayload(untaint payload);
        _ = caller -> respond(res);
    }
}
