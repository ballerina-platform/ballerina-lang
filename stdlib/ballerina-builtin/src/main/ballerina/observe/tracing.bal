// Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.


import ballerina/http;

public type REFERENCE_TYPE "CHILDOF" | "FOLLOWSFROM" | "ROOT";

@final public REFERENCE_TYPE REFERENCE_TYPE_CHILDOF = "CHILDOF";
@final public REFERENCE_TYPE REFERENCE_TYPE_FOLLOWSFROM = "FOLLOWSFROM";
@final public REFERENCE_TYPE REFERENCE_TYPE_ROOT = "ROOT";

@Description {value:"Represents a opentracing span in Ballerina"}
@Field {value:"spanId: The id of the span"}
@Field {value:"serviceName: The service name this span belongs to"}
@Field {value:"spanName: The name of the span"}
public type Span object {

    public {
        @readonly string serviceName,
        @readonly string spanName,
    }

    private {
        string spanId;
    }

    @Description {value:"Finish the span specified by the spanId"}
    public native function finishSpan();

    @Description {value:"Add a tag to the current span. Tags are given as a key value pair"}
    @Param {value:"tagKey: The key of the key value pair"}
    @Param {value:"tagValue: The value of the key value pair"}
    public native function addTag(string tagKey, string tagValue);

    @Description {value:"Add a baggage item to the current span. Baggage items are given as a key value pair"}
    @Param {value:"tagKey: The key of the key value pair"}
    @Param {value:"tagValue: The value of the key value pair"}
    public native function setBaggageItem(string baggageKey, string baggageValue);

    @Description {value:"Add a baggage item to the current span. Baggage items are given as a key value pair"}
    @Param {value:"tagKey: The key of the key value pair"}
    public native function getBaggageItem(string baggageKey) returns string | ();

    @Description {value:"Attach an info log to the current span"}
    @Param {value:"event: The type of event this log represents"}
    @Param {value:"message: The message to be logged"}
    public native function log(string event, string message);

    @Description {value:"Attach an error log to the current span"}
    @Param {value:"errorKind: The kind of error. e.g. DBError"}
    @Param {value:"message: The error message to be logged"}
    public native function logError(string errorKind, string message);

    @Description {value:"Adds span headers when request chaining"}
    @Return {value:"The span context as a key value pair that should be passed out to an external function"}
    public native function injectSpanContext (string traceGroup) returns map;

    @Description {value:"Injects the span context the Request so it can be sent to another service"}
    @Param {value:"req: The http request used when calling an endpoint"}
    @Param {value:"traceGroup: The group that the span context is associated to"}
    @Return {value:"The http request which includes the span context related headers"}
    public function injectSpanContextToHttpHeader (http:Request req, string traceGroup) returns http:Request {
        map headers = self.injectSpanContext(traceGroup);
        foreach key, v in headers {
            var value = <string>v;
            req.addHeader(key, value);
            }
        return req;
    }
};


@Description {value:"Holds the spancontext map of opentracing implementations. Used as a parent span across services.
                SpanContext is just a representation of a propogated Span and can not be finished."}
@Field {value:"contextMap: The map containing information to build span contexts of opentracing implementations"}
public type SpanContext {
    map<string> contextMap,
};

@Description {value:"Starts a span and sets the specified reference to parent span"}
@Param {value:"serviceName: The service name of the process"}
@Param {value:"spanName: The name of the span"}
@Param {value:"tags: The map of tags to be associated to the span"}
@Param {value:"reference: childOf, followsFrom, root"}
@Param {value:"parentSpan: Can be of type Span or SpanContext. If no parent null can be given"}
@Return {value:"The newly started span"}
public function startSpan(string serviceName, string spanName, map | () tags, REFERENCE_TYPE reference,
                                                                Span | SpanContext | () parentSpan) returns Span {
    match parentSpan {
        Span span => return startSpanWithParentSpan(serviceName, spanName, tags, reference, span);
        SpanContext spanContext => {
            return startSpanWithParentContext(serviceName, spanName, tags, reference, spanContext);
        }
        () => return startRootSpan(serviceName, spanName, tags);
    }
}

@Description {value:"Creates a span context of a parent span propogated through request chaining"}
@Param {value:"headers: The map of headers"}
@Param {value:"traceGroup: The kind of error. e.g. DBError"}
@Return {value:"The SpanContext that was propogated from another service"}
public native function extractSpanContext (map headers, string traceGroup) returns SpanContext;

@Description {value:"Method to save the parent span and extract the span Id"}
@Param {value:"req: The http request that contains the header maps"}
@Param {value:"traceGroup: The group to which this span belongs to"}
@Return {value:"The SpanContext that was propogated from another service"}
public function extractSpanContextFromHttpHeader (http:Request req, string traceGroup) returns SpanContext {
    map<string[]> headers;
    string[] headerNames = req.getHeaderNames();
    foreach headerName in headerNames {
        if (req.hasHeader(headerName)) {
            string[] headerValues = req.getHeaders(untaint headerName);
            headers[headerName] = headerValues;
        }
    }
    return extractSpanContext(headers, traceGroup);
}

@Description {value:"Start a root span which has no parent"}
@Param {value:"serviceName: The service name of the process"}
@Param {value:"spanName: The name of the span"}
@Param {value:"tags: The map of tags to be associated to the span"}
@Return {value:"The newly started span"}
native function startRootSpan(string serviceName, string spanName, map | () tags) returns Span;

@Description {value:"Starts a span and sets the specified reference to parent span"}
@Param {value:"serviceName: The service name of the process"}
@Param {value:"spanName: The name of the span"}
@Param {value:"tags: The map of tags to be associated to the span"}
@Param {value:"reference: childOf, followsFrom, root"}
@Param {value:"parentSpan: Instance of Span to be referenced to"}
@Return {value:"The newly started span"}
native function startSpanWithParentSpan(string serviceName, string spanName, map | () tags,
                                                            REFERENCE_TYPE reference, Span parentSpan) returns Span;

@Description {value:"Starts a span and sets the specified reference to parent span"}
@Param {value:"serviceName: The service name of the process"}
@Param {value:"spanName: The name of the span"}
@Param {value:"tags: The map of tags to be associated to the span"}
@Param {value:"reference: childOf, followsFrom, root"}
@Param {value:"parentSpanContext: Instance of SpanContext to be referenced to"}
@Return {value:"The newly started span"}
native function startSpanWithParentContext(string serviceName, string spanName, map | () tags,
                                                REFERENCE_TYPE reference, SpanContext parentSpanContext) returns Span;
