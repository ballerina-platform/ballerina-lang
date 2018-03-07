import ballerina.task;

int count;

function scheduleAppointment(string cronExpression) returns (string taskId, error e) {
    function () returns (error) onTriggerFunction;
    function (error e) onErrorFunction;

    onTriggerFunction = onTrigger;
    onErrorFunction = cleanupError;
    taskId, e = task:scheduleAppointment(onTriggerFunction, onErrorFunction, cronExpression);
    return;
}

function getCount() returns (int) {
    return count;
}

function onTrigger() returns (error e) {
    count = count + 1;
    return;
}

function cleanupError(error e) {

}

function stopTask (string taskId) returns (error stopError) {
    stopError = task:stopTask(taskId);
    if(stopError == null) {
        count = -1;
    }
    return;
}
