import ballerina/http;
import ballerina/log;
import ballerina/observe;
import ballerina/runtime;

// Make sure you start the service with `--observe`, or tracing enabled.
service<http:Service> hello bind { port: 9234 } {

    // Invoke all resources with arguments of server connector and request.
    sayHello(endpoint caller, http:Request req) {
        http:Response res = new;

        //Start a child span attaching to the system span generated.
        int spanId = check observe:startSpan("MyFirstLogicSpan");

        //Start a new root span without attaching to the system span.
        int rootParentSpanId = observe:startRootSpan("MyRootParentSpan");
        // Some actual logic will go here, and for example we have introduced some delay with sleep.
        runtime:sleep(1000);
        //Start a new child span for the span `MyRootParentSpan`.
        int childSpanId = check observe:startSpan("MyRootChildSpan", parentSpanId = rootParentSpanId);
        // Some actual logic will go here, and for example we have introduced some delay with sleep.
        runtime:sleep(1000);
        //Finish `MyRootChildSpan` span.
        _ = observe:finishSpan(childSpanId);
        // Some actual logic will go here, and for example we have introduced some delay with sleep.
        runtime:sleep(1000);
        //Finish `MyRootParentSpan` span.
        _ = observe:finishSpan(rootParentSpanId);

        //Some actual logic will go here, and for example we have introduced some delay with sleep.
        runtime:sleep(1000);

        //Finish the created child span `MyFirstLogicSpan`, which was attached to the system trace.
        _ = observe:finishSpan(spanId);

        //Use a util method to set a string payload.
        res.setPayload("Hello, World!");

        //Send the response back to the caller.
        caller->respond(res) but { error e => log:printError(
                           "Error sending response", err = e) };
    }
}
