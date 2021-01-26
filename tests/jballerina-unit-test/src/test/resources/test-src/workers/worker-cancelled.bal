import ballerina/jballerina.java;

// Test if worker actions are panicked if the worker is cancelled before sending
function workerCancelledBeforeSend() {
        @strand{thread:"any"}
        worker wy {
            string aa = <- default;
            "message" -> default;
        }
        wy.cancel();
        sleep(5);
        "message" -> wy;
        string|error result = <- wy;
}

public function sleep(int millis) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Sleep"
} external;
