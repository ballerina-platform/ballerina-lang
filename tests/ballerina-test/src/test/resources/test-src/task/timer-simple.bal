import ballerina/task;

int count;

function scheduleTimer (int delay, int interval) returns (string|error) {
    function () returns (error?) onTriggerFunction = onTrigger;
    return check task:scheduleTimer(onTriggerFunction, (()), {delay:delay, interval:interval});
}

function getCount () returns (int) {
    return count;
}

function onTrigger () returns (error?) {
    count = count + 1;
    return ();
}

function stopTask (string taskId) returns (error?) {
    _ = check task:stopTask(taskId);
    count = -1;
    return ();
}
