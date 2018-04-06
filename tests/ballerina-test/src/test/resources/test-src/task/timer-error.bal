import ballerina/task;

error err;
string origErrMsg;

function scheduleTimerWithError(int delay, int interval, string errMsg) returns string {
    origErrMsg = errMsg;
    (function() returns error?) onTriggerFunction = triggerWithError;
    (function(error)) onErrorFunction = onError;
    //(function(error)) onErrorFunction = ();
    return task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:delay, interval:interval});
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

function stopTask(string taskId) returns error? {
    return task:stopTask(taskId);
}
