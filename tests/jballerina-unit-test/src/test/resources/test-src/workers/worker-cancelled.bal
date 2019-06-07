import ballerina/runtime;

// Test if worker actions are panicked if the worker is cancelled before sending
function workerCancelledBeforeSend() {
        worker wy {
            runtime:sleep(1000);
            "message" -> default;
        }
        wy.cancel();
        runtime:sleep(1000);
        string|error result = <- wy;
}
