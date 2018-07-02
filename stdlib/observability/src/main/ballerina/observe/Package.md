## Package Overview

This package provides apis for observing Ballerina services.
Ballerina supports Observability out of the box. This package provides user api's to make Ballerina Observability more flexible for the user.

To observe ballerina code, the `--observe` flag should be given when starting the service. i.e. `ballerina run hello_world.bal --observe`.
For more information on Ballerina Observability visit [How to Observe Ballerina Services](https://ballerina.io/learn/how-to-observe-ballerina-code/)

## Tracing

### Samples

#### Start a root span & attach a child span

The following code snippet show an example of how start a root span with no parent and start another span as a child of the first span.
Note: Make sure that all started spans are closed properly to ensure that all spans are reported properly.

``` ballerina

int spanId = observe:startRootSpan("Parent Span");

//Do Something

int spanId2 = check observe:startSpan("Child Span", parentSpanId = spanId);

// Do Something

_ = observe:finishSpan(spanId2);

// Do Something

_ = observe:finishSpan(spanId);
```

#### Start a span attached to a system trace

When no parentSpanId is given or a parentSpanId of -1 is given, a span is started as a child span to the current active span in the ootb system trace.

``` ballerina
int spanId = check observe:startSpan("Child Span");

// Do Something

_ = observe:finishSpan(spanId);
```

#### Attach a tag to a span

It is possible to add tags to span by using the `observe:addTagToSpan()` api by providing the span id and relevant tag key and tag value.

``` ballerina
_ = observe:addTagToSpan(spanId, "Tag Key", "Tag Value");
```

