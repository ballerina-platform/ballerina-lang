import ballerina/http;
import ballerina/mime;
import ballerina/log;

final string ACCEPT_ENCODING = "accept-encoding";

listener http:Listener passthroughEP2 = new(9091);

http:Client acceptEncodingAutoEP = new("http://localhost:9091/hello", config = {
    compression:http:COMPRESSION_AUTO
});

http:Client acceptEncodingEnableEP = new("http://localhost:9091/hello", config = {
    compression:http:COMPRESSION_ALWAYS
});

http:Client acceptEncodingDisableEP = new("http://localhost:9091/hello", config = {
    compression:http:COMPRESSION_NEVER
});

service passthrough on passthroughEP2 {
    @http:ResourceConfig {
        path:"/"
    }
    resource function passthrough(http:Caller caller, http:Request req) {
        if (req.getHeader("AcceptValue") == "auto") {
            var clientResponse = acceptEncodingAutoEP -> post("/",untaint req);
            if (clientResponse is http:Response) {
                var responseError = caller->respond(clientResponse);
                if (responseError is error) {
                    log:printError("Error sending response", err = responseError);
                }
            } else {
                http:Response res = new;
                res.statusCode = 500;
                res.setPayload(clientResponse.reason());
                var responseError = caller->respond(res);
                if (responseError is error) {
                    log:printError("Error sending response", err = responseError);
                }
            }
        } else if (req.getHeader("AcceptValue") == "enable") {
            var clientResponse = acceptEncodingEnableEP -> post("/",untaint req);
            if (clientResponse is http:Response) {
                _ = caller -> respond(clientResponse);
            } else  {
                http:Response res = new;
                res.statusCode = 500;
                res.setPayload(clientResponse.reason());
                var responseError = caller->respond(res);
                if (responseError is error) {
                    log:printError("Error sending response", err = responseError);
                }
            }
        } else if (req.getHeader("AcceptValue") == "disable") {
            var clientResponse = acceptEncodingDisableEP -> post("/",untaint req);
            if (clientResponse is http:Response) {
                _ = caller->respond(clientResponse);
            } else {
                http:Response res = new;
                res.statusCode =500;
                res.setPayload(clientResponse.reason());
                var responseError = caller->respond(res);
                if (responseError is error) {
                    log:printError("Error sending response", err = responseError);
                }
            }
        }
    }
}

#
# Sample hello world service.
#
service hello on passthroughEP2 {

    #
    # The helloResource only accepts requests made using the specified HTTP methods
    #
    @http:ResourceConfig {
        methods:["POST", "PUT", "GET"],
        path:"/"
    }
    resource function helloResource(http:Caller caller, http:Request req) {
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
