package ballerina.observe;

import ballerina.net.http;
import ballerina.log;

@Description {value:"Represents a opentracing span in Ballerina"}
@Field {value:"spanId: The Id of the Span"}
public struct Span {
    string spanId;
    string serviceName;
    string spanName;
}

@Description {value:"Starts a span and sets the specified reference to parentSpanId"}
@Param {value:"serviceName: The service name of the process"}
@Param {value:"spanName: The name of the span"}
@Param {value:"tags: The map of tags to be associated to the span"}
@Param {value:"reference: childOf, followsFrom"}
@Param {value:"parentSpanId: The Id of the parent span"}
@Return {value:"The span struct"}
public function startSpan (string serviceName, string spanName, map tags, ReferenceType reference, string parentSpanId) (Span) {
    Span span = {};
    span.spanId = init(serviceName, spanName, tags, reference, parentSpanId);
    span.serviceName = serviceName;
    span.spanName = spanName;
    return span;
}

@Description {value:"Injects the span context the OutRequest struct to send to another service"}
@Param {value:"req: The http request used when calling an endpoint"}
@Param {value:"group: The group that the span context is associated to"}
@Return {value:"The http request which includes the span context related headers"}
public function <Span span> injectSpanContext (http:OutRequest req, string group) (http:OutRequest) {
    map headers = span.inject();
    foreach key, v in headers {
        var value, _ = (string)v;
        req.addHeader(group + key, value);
    }
    return req;
}

@Description {value:"Logs and error associated to the span"}
@Param {value:"errorKind: The type or kind of an error. For example DBError"}
@Param {value:"message: The error message"}
public function <Span span> logInfo (map log) {
    log:printInfo("[Tracing] [" + span.serviceName + "].[" + span.spanName + "] " + message);
    span.log(log);
}

@Description {value:"Logs and error associated to the span"}
@Param {value:"errorKind: The type or kind of an error. For example DBError"}
@Param {value:"message: The error message"}
public function <Span span> logError (string errorKind, string message) {
    log:printError("[Tracing] [" + span.serviceName + "].[" + span.spanName + "] " + message);
    span.addTag("error", "true");
    span.log({"event":"error", "error.kind":errorKind, "message":message});
}