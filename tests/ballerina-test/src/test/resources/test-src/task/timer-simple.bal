import ballerina.task;

int count;

function scheduleTimer(int delay, int interval) returns (string taskId, error e) {
    function() returns (error) onTriggerFunction = onTrigger;
    taskId, e = task:scheduleTimer(onTriggerFunction, null, {delay:delay, interval:interval});
    return;
}

function getCount() returns (int) {
    return count;
}

function onTrigger() returns (error e) {
    count = count + 1;
    return;
}

function stopTask (string taskId) returns (error stopError) {
    stopError = task:stopTask(taskId);
    if(stopError == null) {
        count = -1;
    }
    return;
}
