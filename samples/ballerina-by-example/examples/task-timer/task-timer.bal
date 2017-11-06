import ballerina.task;
import ballerina.math;

int count;

function main (string[] args) {
    println("Timer task demo");

    // the cleanup function will be called every time the timer goes off.
    function () returns (error) onTriggerFunction = cleanup;

    // the cleanup error function will be called if an error occurs while cleaning up.
    function (error e) onErrorFunction = cleanupError;

    // Schedule a timer task which initially runs 500ms from now and there
    //onwards runs every 1000ms.
    var taskId, schedulerError = task:scheduleTimer(onTriggerFunction,
                                         onErrorFunction, {delay:500, interval:1000});
    if (schedulerError != null) {
        println("Timer scheduling failed: " + schedulerError.msg) ;
    } else {
        println("Task ID:" + taskId);
    }
}

function cleanup() returns (error e) {
    count = count + 1;
    println("Cleaning up...");
    println(count);

    // We randomly return an error to demonstrate how the error is propagated to the
     //onError function when an error occurs in the onTrigger function.
    if(math:randomInRange(0,10) == 5) {
        e = {msg:"Cleanup error"};
    }
    return;
}

function cleanupError(error e) {
    print("[ERROR] cleanup failed");
    println(e);
}
