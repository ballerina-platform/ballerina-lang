## Package overview

This package provides APIs for using the capabilities of Metrics and Tracing.

### Metrics

The following 4 types of metrics are supported: 

* Counter
* Gauge
* Summary
* Timer

#### Counter 

A `counter` is used to hold a single numerical value that can only be incremented. This can be used to track counts of events or running totals such as the total number of successful requests, or the total number of 5xx server errors.

The following code snippet demonstrates how to create a `counter` with tags, and a few example operations that can be performed on it:

```ballerina
//Create a tag. 
map tags = {"event_type":"test"};

//Create a counter.
observe:Counter counter = new("event_total", "Total count of events.", tags);

//Increment the counter by one.
counter.incrementByOne();

//Increment the counter by five.
counter.increment(5);

//Print the current value of the counter.
io:println("count: " + counter.count());
```

#### Gauge

A `gauge` is used to hold a single numerical value that can be incremented as well as decremented. It can be used to report instantaneous values such as CPU usage and the maximum number of open file descriptors.

The following code demonstrates how to create a `gauge` with tags, and a few example operations that can be performed on it:

```ballerina
//Create a tag.
map tags = {"event_type":"test"};

//Create a gauge.
observe:Gauge gauge = new("event_queue_size", "Size of an event queue.", tags);

//Increment the gauge by one.
gauge.incrementByOne();

//Increment the gauge by three.
gauge.increment(3);

//Decrement the gauge by one.
gauge.decrementByOne();

//Decrement the gauge by two.
gauge.decrement(2);

//Print the current value of the gauge. 
io:println("gauge: " + gauge.value());
```

#### Summary 

A `summary` is used to sample the size of events, which means it can calculate the distribution of a value. For example, request duration and response size. It supports returning the count of recorded events, and returning the maximum and mean values of events recorded. Furthermore, it can return values at different percentiles.

The following code demonstrates how to create a `summary` with tags, and a few example operations that can be performed on it:
```ballerina
//Create a tag.
map tags = {"event_type":"test"};

//Create a summary.
observe:Summary summary = new("event_size", "Size of an event.", tags);

//Record events in the summary.
summary.record(5);    
summary.record(1);
summary.record(8);
summary.record(3);
summary.record(4);

//Count the number of recorded events in the summary.
io:println("count : " + summary.count());

//Return the maximum value of events recorded in the summary.
io:println("max: " + summary.max());

//Return the mean value of events recorded in the summary.
io:println("mean: " + summary.mean());

//Return the values at different percentiles. 
io:print("percentile values: ");
io:println(summary.percentileValues());
io:println("");
```

#### Timer
A `timer` is similar to a `summary`, except it is especially used to sample the time durations. This means that it can aggregate timing durations and provide duration statistics. Similar to a `summary`, it supports returning the count of recorded items and returning the maximum and mean values of events recorded. Additionally, it also supports returning values at different percentiles. A `timer` uses a `summary` internally to provide these features.

The following code demonstrates how to create a `timer` with tags, and a few example operations that can be performed on it:

```ballerina
//Create a timer.
observe:Timer timer = new("event_duration", "Duration of an event.", tags);

//Record times in the timer.
timer.record(1000, observe:TIME_UNIT_NANOSECONDS);
timer.record(200, observe:TIME_UNIT_MICROSECONDS);
timer.record(30, observe:TIME_UNIT_MILLISECONDS);
timer.record(4, observe:TIME_UNIT_SECONDS);
timer.record(1, observe:TIME_UNIT_MINUTES);

//Return the number of times that the record has been called since this timer was created.
io:println("count: " + timer.count());

//Return the maximum time recorded in the timer.
io:println("max: " + timer.max(observe:TIME_UNIT_SECONDS) + " seconds");

//Return the mean value of times recorded in the timer.
io:println("mean: " + timer.mean(observe:TIME_UNIT_SECONDS) + " seconds");

//Return the latencies at specific percentiles.
io:print("percentile values: ");
io:println(timer.percentileValues(observe:TIME_UNIT_SECONDS));
```

**NOTE:** By default, these metrics are exposed via an endpoint that supports the `Prometheus` format. That being said, metric implementation is capable of supporting other monitoring solutions by extending the Ballerina runtime.

### Tracing
This package exposes APIs according to the [OpenTracing specification version 1.1](https://github.com/opentracing/specification/blob/master/specification.md). By default , the [Jaeger](https://github.com/jaegertracing) OpenTracing implementation is used. 

#### Span
According to the OpenTracing specification, tracing is built around the key concept of a `span`. A `span` encapsulates a start time, finish time, a set of span gags, a set of span logs, a span context, and a reference to a parent span. A single `trace` could consist of multiple spans, especially when the `trace` goes across multiple services.

#### Reference Type
The following types of references can exist between spans:
`REFERENCE_TYPE_CHILDOF` - The parent span depends on the child span in some capacity.
`REFERENCE_TYPE_FOLLOWSFROM` - The parent span does not depend on the child span.
`REFERENCE_TYPE_ROOT` - The root span, which has no reference to a parent span.

#### Span Tags
These are key:value pairs that can be used to maintain metadata of the `span`. The keys must be of type string, and the values can be strings, booleans, or numeric types. There is a set of standard tags documented under the [OpenTracing Semantic Conventions](https://github.com/opentracing/specification/blob/master/semantic_conventions.md)

The following code demonstrates an example of starting spans with `REFERENCE_TYPE_ROOT` and `REFERENCE_TYPE_CHILDOF`, including tags:
```ballerina
//Start a root span with the service name ‘Store’, the span name ‘Add Item’ and the ‘span.kind’ //tag as ‘server’.
observe:Span span = observe:startSpan("Store", "Add Item", {"span.kind":"server"}, observe:REFERENCE_TYPE_ROOT, ());

//Start a span with a `CHILDOF` reference to the root span with service name ‘Store’, 
//span name ‘Update Manager Connector’. Add ‘span.kind’ tag as ‘client’
observe:Span childSpan = observe:startSpan("Store", "Update Manager Connector",
                      {"span.kind":"client"}, observe:REFERENCE_TYPE_CHILDOF, span);
```

#### Span Logs
This is a key:value map paired with a timestamp. The keys must be strings, though the values may be of any type. There is a set of standard log fields documented under the [OpenTracing Semantic Conventions](https://github.com/opentracing/specification/blob/master/semantic_conventions.md)

The following code shows examples of the usages of span logs:
```ballerina
//Logging any message.
span.log("Something", "Log");

//Logging an error message.
span.logError("Payload Error", payloadError.message);
```

#### Span Context
A `span` has an implementation specific context that can be used to hold data related to the `span`. Depending on the OpenTracing implementation, a `span context` could have data such as trace ID, span ID, and Baggage items, which are key:value pairs that can be propagated among different services. Jaeger is the default implementation used, and it supports those mentioned data.

#### Trace Group
A `trace group` is a set of related spans across different services. By defining trace groups, the user can start multiple spans of different traces within a service and then propagate those span contexts to other services. Related spans are associated with the trace via the trace group ID.

The following code demonstrates examples of injecting a span context to HTTP headers and retrieving the context back. Usually the insertion is done from the calling service, while the retrieval is done from the called service:

```ballerina
//In order to propagate the span across http requests, the span should be injected to the http headers as a span context.
http:Request outRequest = childSpan.injectSpanContextToHttpHeader(new http:Request(), "group-1");

//Extracting the span context received via http.
observe:SpanContext parentSpanContext = observe:extractSpanContextFromHttpHeader(req, "group-1");
```
