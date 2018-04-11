import ballerina/io;
import ballerina/task;
import ballerina/math;

int count;

function main (string[] args) {
    io:println("Timer task demo");

    // the cleanup function will be called every time the timer goes off.
    function () returns (error|()) onTriggerFunction = cleanup;

    // the cleanup error function will be called if an error occurs while cleaning up.
    function (error e) onErrorFunction = cleanupError;

    // Schedule a timer task which initially runs 500ms from now and there
    //onwards runs every 1000ms.
    string taskId = task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:500, interval:1000});
    io:println("Task ID:" + taskId);
}

function cleanup () returns (error|()) {
    count = count + 1;
    io:println("Cleaning up...");
    io:println(count);

    // We randomly return an error to demonstrate how the error is propagated to the
    //onError function when an error occurs in the onTrigger function.
    if (math:randomInRange(0, 10) == 5) {
        error e = {message:"Cleanup error"};
        return e;
    }
    return ();
}

function cleanupError (error e) {
    io:print("[ERROR] cleanup failed");
    io:println(e);
}
