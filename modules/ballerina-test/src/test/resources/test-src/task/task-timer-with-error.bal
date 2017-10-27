import ballerina.task;
import ballerina.utils.logger;

function scheduleTimer (int delay, int interval, int sleepInterval) returns (int) {

    int schedulerTaskId = -1;
    any schedulerError;
    task:TimerScheduler ts = {delay:delay, interval:interval};
    function () returns (error) onTriggerFunction;
    onTriggerFunction = returnError;
    function (error) onErrorFunction;
    onErrorFunction = errorFunction;

    schedulerTaskId, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, ts);
    var timerSchedulerErrorMessage, castErrorTS = (string)schedulerError;
    if (timerSchedulerErrorMessage != "null" && timerSchedulerErrorMessage != "") {
        logger:error("Timer scheduling failed: " + timerSchedulerErrorMessage);
    }

    sleep(sleepInterval);

    return schedulerTaskId;
}

function returnError () returns (error) {
    int i = 0;
    while(i < 10000) {
        i = i + 10;
    }
    error err = {msg:"Returning error"};
    return err;
}

function errorFunction (error error) {
    if (error != null) {
        logger:error("Error: " + error.msg);
    }
}
