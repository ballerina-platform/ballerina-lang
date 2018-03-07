import ballerina.task;
import ballerina.io;

int w1Count;
int w2Count;
error errorW1;
error errorW2;
string errorMsgW1;
string errorMsgW2;

function scheduleTimer (int w1Delay, int w1Interval, int w2Delay, int w2Interval, string errMsgW1, string errMsgW2)
returns (string w1TaskId, string w2TaskId) {
    worker default {
        errorMsgW1 = errMsgW1;
        errorMsgW2 = errMsgW2;
        w1TaskId <- w1;
        w2TaskId <- w2;
        return;
    }
    worker w1 {
        function () returns (error) onTriggerFunction = onTriggerW1;
        string w1TaskIdX;
        if(errMsgW1 == ""){
            w1TaskIdX, _= task:scheduleTimer(onTriggerFunction, null, {delay:w1Delay, interval:w1Interval});
        } else {
            function (error) onErrorFunction = onErrorW1;
            w1TaskIdX, _ = task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:w1Delay, interval:w1Interval});
        }
        w1TaskIdX -> default;
    }
    worker w2 {
        function () returns (error) onTriggerFunction = onTriggerW2;
        string w2TaskIdX;
        if(errMsgW2 == ""){
            w2TaskIdX, _= task:scheduleTimer(onTriggerFunction, null, {delay:w2Delay, interval:w2Interval});
        } else {
            function (error) onErrorFunction = onErrorW2;
            w2TaskIdX, _= task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:w2Delay, interval:w2Interval});
        }
        w2TaskIdX -> default;
    }
}

function onTriggerW1 () returns (error e) {
    w1Count = w1Count + 1;
    io:println("w1:onTriggerW1");
    if(errorMsgW1 != "") {
        io:println("w1:onTriggerW1 returning error");
        e = {message:errorMsgW1};
    }
    return;
}

function onErrorW1(error e) {
    io:println("w1:onErrorW1");
    errorW1 = e;
}

function onTriggerW2 () returns (error e) {
    w2Count = w2Count + 1;
    io:println("w2:onTriggerW2");
    if(errorMsgW2 != "") {
        io:println("w2:onTriggerW2 returning error");
        e = {message:errorMsgW2};
    }
    return;
}

function onErrorW2(error e) {
    io:println("w2:onErrorW2");
    errorW2 = e;
}

function getCounts () returns (int, int) {
    return w1Count, w2Count;
}

function getErrors() returns (string w1ErrMsg, string w2ErrMsg) {
    if(errorW1 != null) {
        w1ErrMsg = errorW1.message;
    }
    if(errorW2 != null) {
        w2ErrMsg = errorW2.message;
    }
    return;
}

function stopTasks (string w1TaskId, string w2TaskId) returns (error w1StopError, error w2StopError) {
    w1StopError = task:stopTask(w1TaskId);
    if(w1StopError == null) {
        w1Count = -1;
    }
    w2StopError = task:stopTask(w2TaskId);
    if(w2StopError == null) {
        w2Count = -1;
    }
    return;
}
