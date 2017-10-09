import ballerina.lang.system;
import ballerina.task;

function scheduleTimer (int delay, int interval) returns (int) {

    int schedulerTaskId = -1;
    any schedulerError;
    task:TimerScheduler ts = {delay:delay, interval:interval};
    function () returns (json) onTriggerFunction;
    onTriggerFunction = returnDummyMessage;
    function () returns (any) onErrorFunction;
    onErrorFunction = null;

    schedulerTaskId, schedulerError = task:scheduleTimer(onTriggerFunction, onErrorFunction, ts);
    system:println(schedulerError);
    var timerSchedulerErrorMessage, castErrorTS = (string)schedulerError;
    if (timerSchedulerErrorMessage != "null" && timerSchedulerErrorMessage != "") {
        system:println("Timer scheduling failed: " + timerSchedulerErrorMessage);
    }

    system:sleep(40000);

    return schedulerTaskId;
}

function returnDummyMessage () returns (json) {
    int i = 0;
    while(i < 10000) {
        i = i + 10;
    }
    system:println("Loop completed " + i);
    json dummyJSON = {name:"Foo", address:"Bar"};
    return dummyJSON;
}