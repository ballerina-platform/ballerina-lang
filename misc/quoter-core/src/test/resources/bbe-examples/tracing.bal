import ballerina/http;
import ballerina/log;
import ballerina/observe;
import ballerina/runtime;

// Simple `Hello` HTTP Service
service hello on new http:Listener(9234) {

    // Resource functions are invoked with the HTTP caller and the
    // incoming request as arguments.
    resource function sayHello(http:Caller caller, http:Request req)
                                returns error? {
        http:Response res = new;

        //Start a child span attaching to the system span generated.
        int spanId = check observe:startSpan("MyFirstLogicSpan");

        //Start a new root span without attaching to the system span.
        int rootParentSpanId = observe:startRootSpan("MyRootParentSpan");
        // Some actual logic will go here, and for example we have introduced some delay with sleep.
        runtime:sleep(1000);
        //Start a new child span for the span `MyRootParentSpan`.
        int childSpanId = check observe:startSpan("MyRootChildSpan", (),
                                                            rootParentSpanId);
        // Some actual logic will go here, and for example we have introduced some delay with sleep.
        runtime:sleep(1000);
        //Finish `MyRootChildSpan` span.
        error? result = observe:finishSpan(childSpanId);
        if (result is error) {
            log:printError("Error in finishing span", result);
        }
        // Some actual logic will go here, and for example we have introduced some delay with sleep.
        runtime:sleep(1000);
        //Finish `MyRootParentSpan` span.
        result = observe:finishSpan(rootParentSpanId);
        if (result is error) {
            log:printError("Error in finishing span", result);
        }

        //Some actual logic will go here, and for example we have introduced some delay with sleep.
        runtime:sleep(1000);

        //Finish the created child span `MyFirstLogicSpan`, which was attached to the system trace.
        result = observe:finishSpan(spanId);
        if (result is error) {
            log:printError("Error in finishing span", result);
        }
        //Use a util method to set a string payload.
        res.setPayload("Hello, World!");

        //Send the response back to the caller.
        result = caller->respond(res);

        if (result is error) {
            log:printError("Error sending response", result);
        }

        return ();
    }
}
