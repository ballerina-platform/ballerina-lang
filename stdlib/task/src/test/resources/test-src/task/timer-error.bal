import ballerina/task;

error? err = ();
string origErrMsg = "";
task:Timer? timer = ();

function scheduleTimerWithError(int delay, int interval, string errMsg) {
    origErrMsg = errMsg;
    (function() returns error?) onTriggerFunction = triggerWithError;
    (function(error)) onErrorFunction = onError;
    timer = new task:Timer(onTriggerFunction, onErrorFunction, interval, delay = delay);
    _ = timer.start();
}

function triggerWithError() returns error? {
    error e = error(origErrMsg);
    return e;
}

function onError(error e) {
    err = e;
}

function getError () returns (string) {
    string msg = "";
    if (err is error) {
        msg = err.reason();
    }
    return msg;
}

function stopTask() {
    _ = timer.stop();
}
