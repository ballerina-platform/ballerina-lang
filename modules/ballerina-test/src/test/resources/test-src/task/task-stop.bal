import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function stopTask (int schedulerTaskId, int interval) returns (string) {
    any stopTaskError = task:stopTask(schedulerTaskId);
    sleep(interval);
    var stopTaskErrorMessage, castErrorST = (string)stopTaskError;
    if (stopTaskErrorMessage != "") {
        logger:error("Error while stopping the task: " + stopTaskErrorMessage);
        return "false";
    } else {
        return "true";
    }
}
