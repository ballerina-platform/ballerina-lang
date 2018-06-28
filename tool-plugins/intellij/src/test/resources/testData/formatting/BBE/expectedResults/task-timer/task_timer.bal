import ballerina/io;
import ballerina/task;
import ballerina/math;
import ballerina/runtime;

int count;
task:Timer? timer;

function main(string... args) {
    io:println("Timer task demo");

    // The cleanup function is called every time the timer goes off.
    (function() returns error?) onTriggerFunction = cleanup;

    // The cleanup error function is called if an error occurs while cleaning up.
    function(error) onErrorFunction = cleanupError;

    // Schedule a timer task, which initially runs 500ms from now.
    //After that, it runs every 1000ms.
    timer = new task:Timer(onTriggerFunction, onErrorFunction,
        1000, delay = 500);

    // Start the timer.
    timer.start();

    runtime:sleep(30000); // Temp. workaround to stop the process from exiting.
}

function cleanup() returns error? {
    count = count + 1;
    io:println("Cleaning up...");
    io:println(count);

    // An error is randomly returned to demonstrate how the error is propagated
    // to the 'onError' function when an error occurs in the 'onTrigger'
    // function.
    if (math:randomInRange(0, 10) == 5) {
        error e = { message: "Cleanup error" };
        return e;
    }

    if (count >= 10) {

        // This is how you stop a timer.
        timer.stop();
        io:println("Stopped timer");
    }
    return ();
}

function cleanupError(error e) {
    io:print("[ERROR] cleanup failed");
    io:println(e);
}
