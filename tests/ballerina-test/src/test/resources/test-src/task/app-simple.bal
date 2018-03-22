import ballerina/task;

int count;

function scheduleAppointment (string cronExpression) returns (string|error) {
    function () returns (error|null) onTriggerFunction = onTrigger;
    function (error e)|null onErrorFunction = cleanupError;
    return task:scheduleAppointment(onTriggerFunction, onErrorFunction, cronExpression);
}

function getCount () returns (int) {
    return count;
}

function onTrigger () returns (error|null) {
    count = count + 1;
    return null;
}

function cleanupError (error e) {

}

function stopTask (string taskId) returns (error|null) {
    error stopError = task:stopTask(taskId);
    if (stopError == null) {
        count = -1;
    }
    return null;
}
