import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function stopTask (int schedulerTaskId, int interval) returns (string) {
    system:println(schedulerTaskId);
    any stopTaskError = task:stopTask(schedulerTaskId);
    system:sleep(interval);
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
