import ballerina.task;
import ballerina.utils.logger;

function main (string[] args) {

    int schedulerTaskId;
    any e1;
    schedulerTaskId, e1 = <int>args[0];

    any stopTaskError = task:stopTask(schedulerTaskId);
    var stopTaskErrorMessage, castErrorST = (string)stopTaskError;
    if (stopTaskErrorMessage != "") {
        logger:error("Error while stopping the task: " + stopTaskErrorMessage);
    }
}
