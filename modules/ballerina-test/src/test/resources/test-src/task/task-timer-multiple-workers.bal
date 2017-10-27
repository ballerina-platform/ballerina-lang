import ballerina.utils.logger;
import ballerina.task;

function scheduleTimer (int delay, int interval, int sleepInterval) returns (int) {
    worker w1 {
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
    worker w2 {
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
}

function testFunction () returns (error) {
    int i = 0;
    while(i < 10000) {
        i = i + 10;
    }
    logger:info("Loop Completed");
    return null;
}
