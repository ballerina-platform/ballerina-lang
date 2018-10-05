import ballerina/http;
import ballerina/log;

endpoint http:Listener listenerEndpoint {
    port: 9090
};

// Since compression behaviour of the service is set as COMPRESSION_AUTO, entity body compression is done according
// to the scheme indicated in `Accept-Encoding` request header. When the header is not present or the
// header value is "identity", compression is not performed.
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

// COMPRESSION_ALWAYS will guarantee a compressed response entity body. Compression scheme is set to the
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
    // `getJson` resource's response entity body will not get compressed.
    getJson(endpoint caller, http:Request req) {
        caller->respond({ "Type": "Always but constrained by content-type" }) but {
            error e => log:printError("Error sending response", err = e)
        };
    }
    //The response entity body will always get compressed as MIME type is matched.
    getString(endpoint caller, http:Request req) {
        caller->respond("Type : This is a string") but {
            error e => log:printError("Error sending response", err = e)
        };
    }
}

// HTTP client can indicate the compression behaviour("AUTO", "ALWAYS", "NEVER") for content negotiation.
// Depending on the compression option values, Accept-Encoding header is sent along with the request.
// In this example, client compression behaviour is set as COMPRESSION_ALWAYS. If user has not specified
// Accept-Encoding header, client will specify it with "deflate, gzip". Otherwise existing header is sent.
// When compression is specified as COMPRESSION_AUTO, only the user specified Accept-Encoding header is sent.
// If behaviour is set as COMPRESSION_NEVER, client will make sure not to send Accept-Encoding header.
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

// The compression behaviour of service is inferred by its default value that is COMPRESSION_AUTO
service<http:Service> backend bind listenerEndpoint {
    echo(endpoint caller, http:Request req) {
        string value;
        http:Response res = new;
        if (req.hasHeader("accept-encoding")) {
            value = req.getHeader("accept-encoding");
            res.setPayload("Backend response was encoded : " + untaint value);
        } else {
            res.setPayload("Accept-Encoding header is not present");
        }
        caller->respond(res) but { error e => log:printError("Error sending response", err = e) };
    }
}
