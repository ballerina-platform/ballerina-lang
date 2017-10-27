import ballerina.task;
import ballerina.utils.logger;

function scheduleTimer (int delay, int interval, int sleepInterval)  (int) {

    int schedulerTaskId = -1;
    any schedulerError;
    task:TimerScheduler ts = {delay:delay, interval:interval};
    function () returns (error) onTriggerFunction;
    onTriggerFunction = testFunction;
    function (error) onErrorFunction;
    onErrorFunction = null;

    schedulerTaskId, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, ts);
    var timerSchedulerErrorMessage, castErrorTS = (string)schedulerError;
    if (timerSchedulerErrorMessage != "null" && timerSchedulerErrorMessage != "") {
        logger:error("Timer scheduling failed: " + timerSchedulerErrorMessage);
    }

    sleep(sleepInterval);

    return schedulerTaskId;
}

function testFunction () returns (error) {
    int i = 0;
    while(i < 10000) {
        i = i + 10;
    }
    logger:info("Loop Completed");
    return null;
}
