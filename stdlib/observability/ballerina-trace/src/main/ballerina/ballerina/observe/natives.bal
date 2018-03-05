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

package ballerina.observe;

import ballerina.net.http;
import ballerina.log;

public enum ReferenceType {
    CHILDOF, FOLLOWSFROM
}

@Description {value:"Represents a opentracing span in Ballerina"}
@Field {value:"spanId: The Id of the Span"}
public struct Span {
    string spanId;
    string serviceName;
    string spanName;
}

@Description {value:"Builds a span and sets the specified reference to parentSpanId"}
@Param {value:"serviceName: The service name of the process"}
@Param {value:"spanName: The name of the span"}
@Param {value:"tags: The map of tags to be associated to the span"}
@Param {value:"reference: childOf, followsFrom"}
@Param {value:"parentSpanId: The Id of the parent span"}
@Return {value:"String value of the span id that was generated"}
native function init (string serviceName, string spanName, map tags, ReferenceType reference, string parentSpanId) (string);

@Description {value:"Finish the span specified by the spanId"}
@Param {value:"spanId: The ID of the span to be finished"}
public native function <Span span> finishSpan ();

@Description {value:""}
@Param {value:"tags: The map of tags"}
@Return {value:"The status of the add tag operation"}
public native function <Span span> addTag (string tagKey, string tagValue);

@Description {value:""}
@Param {value:"logEvent: The map that holds the log fields related to a single log"}
@Return {value:"The status of the add tag operation"}
public native function <Span span> log (map logEvent);

@Description {value:"Adds span headers when request chaining"}
@Return {value:"The span context as a key value pair that should be passed out to an external function"}
native function <Span span> inject () (map);

@Description {value:"Method to save the parent span and extract the span Id"}
@Param {value:"req: The http request that contains the header maps"}
@Param {value:"group: The group to which this span belongs to"}
@Return {value:"The id of the parent span passed in from an external function"}
public native function extractSpanContext (http:InRequest req, string group) (string);

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
public function <Span span> logError (string errorKind, string message) {
    log:printError("[Tracing] [" + span.serviceName + "].[" + span.spanName + "] ");
    span.addTag("error", "true");
    span.log({"event":"error", "error.kind":errorKind, "message":message});
}
