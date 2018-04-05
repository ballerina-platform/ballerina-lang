import ballerina/task;

int count;

function scheduleTimer (int delay, int interval) returns (string|error) {
    function () returns (error?) onTriggerFunction = onTrigger;
    return check task:scheduleTimer(onTriggerFunction, (()), {delay:delay, interval:interval});
}

function getCount () returns (int) {
    return count;
}

function onTrigger () returns (error) {
    error e = {};
    count = count + 1;
    return e;
}

function stopTask (string taskId) returns (error) {
    error stopError = task:stopTask(taskId);
    if (stopError == ()) {
        count = -1;
    }
    return stopError;
}
