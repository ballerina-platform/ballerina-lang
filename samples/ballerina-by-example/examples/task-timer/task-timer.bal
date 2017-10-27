import ballerina.lang.system;
import ballerina.task;
import ballerina.math;

int count;

function main (string[] args) {
    system:println("Task test");
    string taskId = scheduleTimer(500, 1000, 200000);
    system:println("Task ID:" + taskId);
}

function scheduleTimer (int delay, int interval, int sleepInterval)  (string) {
    string taskId;
    error schedulerError;
    function () returns (error) onTriggerFunction = cleanup;
    function (error e) onErrorFunction = cleanupError;

    taskId, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:delay, interval:interval});
    if (schedulerError != null) {
        system:println("Timer scheduling failed: " + schedulerError.msg) ;
    }
    return taskId;
}

function cleanup() returns (error e) {
    count = count + 1;
    system:println("Cleaning up");
    system:println(count);
    if(math:randomInRange(0,10) == 5) {
        e = {msg:"Cleanup error"};
    }
    return;
}

function cleanupError(error e) {
    system:print("[ERROR] cleanup failed");
    system:println(e);
}
