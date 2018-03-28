import ballerina/task;

int count;

function scheduleTimer (int delay, int interval) returns (string|error) {
    function () returns (error|null) onTriggerFunction = onTrigger;
    return task:scheduleTimer(onTriggerFunction, null, {delay:delay, interval:interval});
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
    if (stopError == null) {
        count = -1;
    }
    return stopError;
}
