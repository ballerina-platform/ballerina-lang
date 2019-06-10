// Test if worker actions are panicked if the worker is cancelled before sending
function workerCancelledBeforeSend() {
        worker wy {
            string aa = <- default;
            "message" -> default;
        }
        wy.cancel();
        "message" -> wy;
        string|error result = <- wy;
}
