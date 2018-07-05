import ballerina/task;

int count;
task:Timer? timer;

function scheduleTimer(int delay, int interval) {
    (function() returns error?) onTriggerFunction = onTrigger;
    timer = new task:Timer(onTriggerFunction, (), interval, delay = delay);
    _ = timer.start();
}

function getCount() returns (int) {
    return count;
}

function onTrigger() returns error? {
    count = count + 1;
    return ();
}

function stopTask() returns error? {
    _ = timer.stop();
    count = -1;
    return ();
}
