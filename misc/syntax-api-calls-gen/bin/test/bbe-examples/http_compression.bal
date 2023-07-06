import ballerina/http;
import ballerina/log;

listener http:Listener listenerEndpoint = new (9090);

// Since compression behavior of the service is set as [COMPRESSION_AUTO](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/constants.html#COMPRESSION_AUTO),
// entity body compression is done according to the scheme indicated in the `Accept-Encoding` request header.
// Compression is not performed when the header is not present or when the header value is "identity".
// [compression](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/records/CompressionConfig.html) annotation
// provides the related configurations.
@http:ServiceConfig {
    compression: {
        enable: http:COMPRESSION_AUTO
    }
}
service autoCompress on listenerEndpoint {
    @http:ResourceConfig {
        path: "/"
    }
    resource function invokeEndpoint(http:Caller caller, http:Request req) {
        var result = caller->respond({"Type": "Auto compression"});

        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

// [COMPRESSION_ALWAYS](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/constants.html#COMPRESSION_ALWAYS)
// guarantees a compressed response entity body. Compression scheme is set to the
// value indicated in Accept-Encoding request header. When a particular header is not present or the header
// value is "identity", encoding is done using the "gzip" scheme.
// By default, Ballerina compresses any MIME type unless they are mentioned under `contentTypes`.
// Compression can be constrained to certain MIME types by specifying them as an array of MIME types.
// In this example encoding is applied to "text/plain" responses only.
@http:ServiceConfig {
    compression: {
        enable: http:COMPRESSION_ALWAYS,
        contentTypes: ["text/plain"]
    }
}
service alwaysCompress on listenerEndpoint {
    // Since compression is only constrained to "text/plain" MIME type,
    // `getJson` resource does not compress the response entity body.
    resource function getJson(http:Caller caller, http:Request req) {
        json msg = {"Type": "Always but constrained by content-type"};
        var result = caller->respond(msg);
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
    // The response entity body is always compressed since MIME type has matched.
    resource function getString(http:Caller caller, http:Request req) {
        var result = caller->respond("Type : This is a string");
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}

// The HTTP client can indicate the [compression](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/types.html#Compression)
// behavior ("AUTO", "ALWAYS", "NEVER") for content negotiation.
// Depending on the compression option values, the `Accept-Encoding` header is sent along with the request.
// In this example, the client compression behavior is set as [COMPRESSION_ALWAYS](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/constants.html#COMPRESSION_ALWAYS). If you have not specified
// an `Accept-Encoding` header, the client specifies it with "deflate, gzip". Alternatively, the existing header is sent.
// When compression is specified as [COMPRESSION_AUTO](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/constants.html#COMPRESSION_AUTO), only the user-specified `Accept-Encoding` header is sent.
// If the behavior is set as [COMPRESSION_NEVER](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/constants.html#COMPRESSION_NEVER), the client makes sure not to send the `Accept-Encoding` header.
http:Client clientEndpoint = new ("http://localhost:9090", {
        compression: http:COMPRESSION_ALWAYS
    }
);

service passthrough on new http:Listener(9092) {
    @http:ResourceConfig {
        path: "/"
    }
    resource function getCompressed(http:Caller caller, http:Request req) {
        var response = clientEndpoint->post("/backend/echo", <@untainted>req);
        if (response is http:Response) {
            var result = caller->respond(<@untainted>response);
            if (result is error) {
                log:printError("Error sending response", result);
            }
        } else {
            json err = {"error": "error occurred while invoking service"};
            var result = caller->respond(err);
            if (result is error) {
                log:printError("Error sending response", result);
            }
        }
    }
}

// The compression behavior of the service is inferred by [COMPRESSION_AUTO](https://ballerina.io/swan-lake/learn/api-docs/ballerina/http/constants.html#COMPRESSION_AUTO), which is the default value
// of the compression config.
service backend on listenerEndpoint {
    resource function echo(http:Caller caller, http:Request req) {
        http:Response res = new;
        if (req.hasHeader("accept-encoding")) {
            string value = req.getHeader("accept-encoding");
            res.setPayload("Backend response was encoded : " +
                            <@untainted> value);
        } else {
            res.setPayload("Accept-Encoding header is not present");
        }

        var result = caller->respond(res);
        if (result is error) {
            log:printError("Error sending response", result);
        }
    }
}
