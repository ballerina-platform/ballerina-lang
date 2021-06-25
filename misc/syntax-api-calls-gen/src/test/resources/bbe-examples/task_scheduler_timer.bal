import ballerina/io;
import ballerina/lang.runtime;
import ballerina/task;

// Defines a custom record type to use in the timer.
public type CounterContext record {|
    string name;
    int count;
|};

public function main() returns error? {
    // Initializes the timer scheduler using the interval value. The delay will
    // be equal to the interval if an initial delay is not provided.
    task:Scheduler timer = check new ({
        intervalInMillis: 1000,
        initialDelayInMillis: 0
    });

    // Define a `CounterContext` object
    CounterContext counterCtx = {name: "MyCounter", count: 0};

    // Attaching the service to the timer. This will not start the timer.
    // However, it will attach the service to the timer and also provide the
    // `CounterContext` object, that will be passed into the `onTrigger()` resource
    check timer.attach(timerService, counterCtx);

    // Starts the timer.
    check timer.start();

    runtime:sleep(4.5);

    // Cancels the timer. This will stop the timer and all the services
    // attached to it.
    check timer.stop();

    io:println("End.");
}

// The service, which will be attached to the timer.
service object{} timerService = service object {
    // The onTrigger resource, which will be invoked when the timer is triggered.
    remote function onTrigger(CounterContext ctx) {
        ctx.count += 1;
        io:println(ctx.name, ": ", ctx.count);
    }

};
