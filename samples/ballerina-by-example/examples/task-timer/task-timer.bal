import ballerina.task;
import ballerina.math;

int count;

function main (string[] args) {
    println("Task test");
    string taskId = scheduleTimer(500, 1000, 200000);
    println("Task ID:" + taskId);
}

function scheduleTimer (int delay, int interval, int sleepInterval)  (string) {
    string taskId;
    error schedulerError;
    function () returns (error) onTriggerFunction = cleanup;
    function (error e) onErrorFunction = cleanupError;

    taskId, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction,
                                                    {delay:delay, interval:interval});
    if (schedulerError != null) {
        println("Timer scheduling failed: " + schedulerError.msg) ;
    }
    return taskId;
}

function cleanup() returns (error e) {
    count = count + 1;
    println("Cleaning up");
    println(count);
    if(math:randomInRange(0,10) == 5) {
        e = {msg:"Cleanup error"};
    }
    return;
}

function cleanupError(error e) {
    print("[ERROR] cleanup failed");
    println(e);
}
