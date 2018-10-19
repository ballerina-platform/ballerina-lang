import ballerina/http;
import ballerina/log;

endpoint http:Listener listenerEndpoint {
    port: 9090
};

// Since compression behaviour of the service is set as `COMPRESSION_AUTO`, entity body compression is done according
// to the scheme indicated in `Accept-Encoding` request header. Compression is not performed when the header is not
// present or the header value is "identity".
@http:ServiceConfig {
    compression: {
        enable: http:COMPRESSION_AUTO
    }
}
service<http:Service> autoCompress bind listenerEndpoint {
    @http:ResourceConfig {
        path: "/"
    }
    invokeEndpoint(endpoint caller, http:Request req) {
        caller->respond({ "Type": "Auto compression" }) but {
            error e => log:printError("Error sending response", err = e)
        };
    }
}

// `COMPRESSION_ALWAYS` guarantees a compressed response entity body. Compression scheme is set to the
// value indicated in Accept-Encoding request header. When particular header is not present or the header
// value is "identity", encoding is done using "gzip" scheme.
// By default ballerina compresses any MIME type unless certain types are mentioned under "contentTypes".
// Compression can be constrained to certain MIME types by specifying them as an array of MIME types.
// In this example encoding is applied to "text/plain" responses only.
@http:ServiceConfig {
    compression: {
        enable: http:COMPRESSION_ALWAYS,
        contentTypes:["text/plain"]
    }
}
service<http:Service> alwaysCompress bind listenerEndpoint {
    // Since compression is only constrained to "text/plain" MIME type,
    // `getJson` resource does not compress the response entity body.
    getJson(endpoint caller, http:Request req) {
        caller->respond({ "Type": "Always but constrained by content-type" }) but {
            error e => log:printError("Error sending response", err = e)
        };
    }
    // The response entity body is always compressed since MIME type has matched.
    getString(endpoint caller, http:Request req) {
        caller->respond("Type : This is a string") but {
            error e => log:printError("Error sending response", err = e)
        };
    }
}

// The HTTP client can indicate the compression behaviour ("AUTO", "ALWAYS", "NEVER") for content negotiation.
// Depending on the compression option values, the `Accept-Encoding` header is sent along with the request.
// In this example, the client compression behaviour is set as `COMPRESSION_ALWAYS`. If you have not specified
// `Accept-Encoding` header, the client specifies it with "deflate, gzip". Alternatively, the existing header is sent.
// When compression is specified as `COMPRESSION_AUTO`, only the user specified `Accept-Encoding` header is sent.
// If the behaviour is set as `COMPRESSION_NEVER`, the client makes sure not to send the `Accept-Encoding` header.
endpoint http:Client clientEndpoint {
    url: "http://localhost:9090",
    compression: http:COMPRESSION_ALWAYS
};

service<http:Service> passthrough bind { port: 9092 } {
    @http:ResourceConfig {
        path: "/"
    }
    getCompressed(endpoint caller, http:Request req) {
        var result = clientEndpoint->post("/backend/echo", untaint req);
        match result {
            http:Response clientResponse => {
                caller->respond(clientResponse) but {
                    error e => log:printError("Error sending response", err = e)
                };
            }
            error responseError => {
                caller->respond({ "error": "error occurred while invoking the service" }) but {
                    error e => log:printError("Error sending response", err = e) };
            }
        }
    }
}

// The compression behaviour of the service is inferred by `COMPRESSION_AUTO`, which is the default value
// of the compression config
service<http:Service> backend bind listenerEndpoint {
    echo(endpoint caller, http:Request req) {
        http:Response res = new;
        if (req.hasHeader("accept-encoding")) {
            string value = req.getHeader("accept-encoding");
            res.setPayload("Backend response was encoded : " + untaint value);
        } else {
            res.setPayload("Accept-Encoding header is not present");
        }
        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }
}
