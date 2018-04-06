import ballerina/task;

int count;

function scheduleAppointment(string cronExpression) returns (string|error) {
    (function() returns error?) onTriggerFunction = onTrigger;
    (function (error)) onErrorFunction = cleanupError;
    return task:scheduleAppointment(onTriggerFunction, onErrorFunction, cronExpression, true);
}

function getCount() returns (int) {
    return count;
}

function onTrigger() returns error? {
    count = count + 1;
    return ();
}

function cleanupError(error e) {

}

function stopTask(string taskId) returns error? {
    error?stopError = task:stopTask(taskId);
    match stopError {
        error => {}
        () => count = -1;
    }
    return stopError;
}
