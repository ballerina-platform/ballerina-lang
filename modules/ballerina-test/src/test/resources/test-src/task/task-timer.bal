import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function scheduleTimer (int delay, int interval, int sleepInterval) returns (int) {

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

    system:sleep(sleepInterval);

    return schedulerTaskId;
}

function returnDummyMessage () returns (json) {
    int i = 0;
    while(i < 10000) {
        i = i + 10;
    }
    json dummyJSON = {name:"Foo", address:"Bar"};
    logger:info("Sample JSON object is returned");
    return dummyJSON;
}
