import ballerina.lang.system;
import ballerina.task;
import ballerina.utils.logger;

function stopTask (string taskId, int interval) returns (boolean ) {
    any stopTaskError = task:stopTask(taskId);
    system:sleep(interval);
    var stopTaskErrorMessage, castErrorST = (string)stopTaskError;
    if (stopTaskErrorMessage != "") {
        logger:error("Error while stopping the task: " + stopTaskErrorMessage);
        return false;
    } else {
        return true;
    }
}
