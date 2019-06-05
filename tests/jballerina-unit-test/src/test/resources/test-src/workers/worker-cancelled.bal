import ballerina/io;
import ballerina/runtime;

// Test if worker actions are panicked if the worker is cancelled before sending
function workerCancelledBeforeSend() {
    io:println("**insideFuture**");
        io:println("**f-going**");
        worker wy {
            runtime:sleep(100);
            "message" -> default;
        }
        boolean bb = wy.cancel();
        runtime:sleep(10000);
        string|error result = <- wy;
        io:println("**f-going**");
        io:println("**endFuture**");
}

function futureActions() returns boolean {
    io:println("**insideFuture**");
        io:println("**f-going**");
        worker w {
            string|error result = <- default;
        }
        boolean cancelled = w.isCancelled();
        boolean isDone = w.isDone();
        if (!cancelled) {
            boolean bb = w.cancel();
        }
        "message" -> w;
        runtime:sleep(100);
        io:println("**f-going**");
        io:println("**endFuture**");
        return w.isCancelled();
}
