import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function scheduleTimer (int delay, int interval, int sleepInterval) returns (int) {

    int schedulerTaskId = -1;
    any schedulerError;
    task:TimerScheduler ts = {delay:delay, interval:interval};
    function () returns (string) onTriggerFunction;
    onTriggerFunction = returnEmpty;
    function (any) onErrorFunction;
    onErrorFunction = errorFunction;

    schedulerTaskId, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, ts);
    var timerSchedulerErrorMessage, castErrorTS = (string)schedulerError;
    if (timerSchedulerErrorMessage != "null" && timerSchedulerErrorMessage != "") {
        logger:error("Timer scheduling failed: " + timerSchedulerErrorMessage);
    }

    system:sleep(sleepInterval);

    return schedulerTaskId;
}

function returnEmpty () returns (string) {
    int i = 0;
    while(i < 10000) {
        i = i + 10;
    }
    return "";
}

function errorFunction (any error) {
    var errorMessage, castErr = (string)error;
    if (errorMessage != "") {
        logger:error(errorMessage);
    }
}
