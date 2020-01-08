import ballerina/runtime;

// Test if worker actions are panicked if the worker is cancelled before sending
function workerCancelledBeforeSend() {
        worker wy {
            string aa = <- default;
            "message" -> default;
        }
        wy.cancel();
        runtime:sleep(5);
        "message" -> wy;
        string|error result = <- wy;
}
