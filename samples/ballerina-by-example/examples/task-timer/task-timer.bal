import ballerina.lang.system;
import ballerina.task;
import org.wso2.ballerina.connectors.googlespreadsheet;
import ballerina.lang.messages;

function main (string[] args) {

    int schedulerTaskId;
    any schedulerError;
    int delay;
    int interval;
    any e1;
    any e2;
    if (args.length == 1) {
        interval, e2 = <int>args[0];
    } else if (args.length > 1) {
        delay, e1 = <int>args[0];
        interval, e2 = <int>args[1];
    }
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

    system:sleep(100000);

    any stopTaskError = task:stopTask(schedulerTaskId);
    var stopTaskErrorMessage, castErrorST = (string) stopTaskError;
    if (stopTaskErrorMessage != "") {
        system:println("Error while stopping the task: " + stopTaskErrorMessage);
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