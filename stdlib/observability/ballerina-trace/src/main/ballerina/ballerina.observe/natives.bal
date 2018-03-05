package ballerina.observe;

import ballerina.net.http;

public enum ReferenceType {
    CHILDOF, FOLLOWSFROM
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
native function <Span span> log (map logEvent);

@Description {value:"Adds span headers when request chaining"}
@Return {value:"The span context as a key value pair that should be passed out to an external function"}
native function <Span span> inject () (map);

@Description {value:"Method to save the parent span and extract the span Id"}
@Param {value:"req: The http request that contains the header maps"}
@Param {value:"group: The group to which this span belongs to"}
@Return {value:"The id of the parent span passed in from an external function"}
public native function extractSpanContext (http:InRequest req, string group) (string);
