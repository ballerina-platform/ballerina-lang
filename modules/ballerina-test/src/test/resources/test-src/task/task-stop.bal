import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function stopTask (int delay, int interval) returns (string) {
    int schedulerTaskId = -1;
    any schedulerError;
    task:TimerScheduler ts = {delay:delay, interval:interval};
    function () returns (json) onTriggerFunction;
    onTriggerFunction = returnDummyMessage;
    function () returns (any) onErrorFunction;
    onErrorFunction = null;

    schedulerTaskId, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, ts);
    var timerSchedulerErrorMessage, castErrorTS = (string)schedulerError;
    if (timerSchedulerErrorMessage != "null" && timerSchedulerErrorMessage != "") {
        logger:error("Timer scheduling failed: " + timerSchedulerErrorMessage);
    }

    system:sleep(30000);

    any stopTaskError = task:stopTask(schedulerTaskId);
    var stopTaskErrorMessage, castErrorST = (string)stopTaskError;
    if (stopTaskErrorMessage != "") {
        logger:error("Error while stopping the task: " + stopTaskErrorMessage);
        return "false";
    } else {
        return "true";
    }
}

function returnDummyMessage () returns (json) {
    int i = 0;
    while(i < 10000) {
        i = i + 10;
    }
    json dummyJSON = {name:"Foo", address:"Bar"};
    return dummyJSON;
}
