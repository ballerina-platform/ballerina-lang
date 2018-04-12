import ballerina/task;

error err;
string origErrMsg;
task:Timer? timer;

function scheduleTimerWithError(int delay, int interval, string errMsg) {
    origErrMsg = errMsg;
    (function() returns error?) onTriggerFunction = triggerWithError;
    (function(error)) onErrorFunction = onError;
    task:Timer t = new(onTriggerFunction, onErrorFunction, interval, delay = delay);
    t.start();
    timer = t;
}

function triggerWithError() returns error? {
    error e = {message:origErrMsg};
    return e;
}

function onError(error e) {
    err = e;
}

function getError () returns (string) {
    string msg;
    if (err != null) {
        msg = err.message;
    }
    return msg;
}

function stopTask() {
    _ = timer.stop();
}
