import ballerina.task;

error err;
string origErrMsg;

function scheduleTimerWithError(int delay, int interval, string errMsg) returns (string taskId, error e) {
    origErrMsg = errMsg;
    function() returns (error) onTriggerFunction = triggerWithError;
    function(error e) onErrorFunction = onError;
    taskId, e = task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:delay, interval:interval});
    return;
}

function triggerWithError() returns (error e) {
    e = {message: origErrMsg};
    return;
}

function onError(error e) {
    err = e;
}

function getError() returns (string msg) {
    if(err != null) {
       msg = err.message;
    }
    return;
}
