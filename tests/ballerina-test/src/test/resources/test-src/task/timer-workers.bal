import ballerina/task;
import ballerina/io;

int w1Count;
int w2Count;
error errorW1;
error errorW2;
string errorMsgW1;
string errorMsgW2;

function scheduleTimer(int w1Delay, int w1Interval, int w2Delay, int w2Interval, string errMsgW1, string errMsgW2) returns (string, string) {
    worker default {
        string w1TaskId;
        string w2TaskId;
        errorMsgW1 = errMsgW1;
        errorMsgW2 = errMsgW2;
        w1TaskId <- w1;
        w2TaskId <- w2;
        return (w1TaskId, w2TaskId);
    }
    worker w1 {
        (function() returns error?) onTriggerFunction = onTriggerW1;
        string w1TaskIdX;
        io:println("errMsgW1: " + errMsgW1);
        if (errMsgW1 == "") {
            w1TaskIdX = task:scheduleTimer(onTriggerFunction, (), {delay:w1Delay, interval:w1Interval}, true);
        } else {
            function (error) onErrorFunction = onErrorW1;
            w1TaskIdX = task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:w1Delay, interval:w1Interval}, true);
        }
        w1TaskIdX -> default;
    }
    worker w2 {
        (function() returns error?) onTriggerFunction = onTriggerW2;
        string w2TaskIdX;
        if (errMsgW2 == "") {
            w2TaskIdX = task:scheduleTimer(onTriggerFunction, (), {delay:w2Delay, interval:w2Interval}, true);
        } else {
            function (error) onErrorFunction = onErrorW2;
            w2TaskIdX = task:scheduleTimer(onTriggerFunction, onErrorFunction, {delay:w2Delay, interval:w2Interval}, true);
        }
        w2TaskIdX -> default;
    }
}

function onTriggerW1() returns error? {
    w1Count = w1Count + 1;
    io:println("w1:onTriggerW1");
    if (errorMsgW1 != "") {
        io:println("w1:onTriggerW1 returning error");
        error e = {message:errorMsgW1};
        return e;
    }
    return;
}

function onErrorW1(error e) {
    io:println("w1:onErrorW1");
    errorW1 = e;
}

function onTriggerW2() returns error? {
    w2Count = w2Count + 1;
    io:println("w2:onTriggerW2");
    if (errorMsgW2 != "") {
        io:println("w2:onTriggerW2 returning error");
        error e = {message:errorMsgW2};
        return e;
    }
    return;
}

function onErrorW2(error e) {
    io:println("w2:onErrorW2");
    errorW2 = e;
}

function getCounts() returns (int, int) {
    return (w1Count, w2Count);
}

function getErrors() returns (string, string) {
    string w1ErrMsg;
    string w2ErrMsg;
    if (errorW1 != null) {
        w1ErrMsg = errorW1.message;
    }
    if (errorW2 != null) {
        w2ErrMsg = errorW2.message;
    }
    return (w1ErrMsg, w2ErrMsg);
}

function stopTasks(string w1TaskId, string w2TaskId) returns (error?, error?) {
    error? w1StopError = task:stopTask(w1TaskId);
    match w1StopError {
        error err => {}
        () => w1Count = -1;
    }
    error?w2StopError = task:stopTask(w2TaskId);
    match w2StopError {
        error err => {}
        () => w2Count = -1;
    }
    return (w1StopError, w2StopError);
}
