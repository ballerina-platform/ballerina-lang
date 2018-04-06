import ballerina/io;
import ballerina/task;

int w1Count;
error errorW1;
string errorMsgW1;

function scheduleAppointment (string cronExpression, string errMsgW1) returns (string) {
    worker default {
        string w1TaskId;
        errorMsgW1 = errMsgW1;
        w1TaskId <- w1;
        return w1TaskId;
    }
    worker w1 {
        (function() returns error?) onTriggerFunction = onTriggerW1;
        string w1TaskIdX;
        if (errMsgW1 == "") {
            w1TaskIdX = task:scheduleAppointment(onTriggerFunction, null, cronExpression);
        } else {
            function(error) onErrorFunction = onErrorW1;
            w1TaskIdX = task:scheduleAppointment(onTriggerFunction, onErrorFunction, cronExpression);
        }
        w1TaskIdX -> default;
    }
}

function onTriggerW1 () returns error? {
    w1Count = w1Count + 1;
    io:println("w1:onTriggerW1");
    if (errorMsgW1 != "") {
        io:println("w1:onTriggerW1 returning error");
        error e = {message:errorMsgW1};
        return e;
    }
    return ();
}

function onErrorW1 (error e) {
    io:println("w1:onErrorW1");
    errorW1 = e;
}

function getCount () returns (int) {
    return w1Count;
}

function getError () returns (string) {
    string w1ErrMsg;
    if (errorW1 != null) {
        w1ErrMsg = errorW1.message;
    }
    return w1ErrMsg;
}

function stopTask (string w1TaskId) returns error? {
    error? w1StopError = task:stopTask(w1TaskId);
    match w1StopError {
        error => {}
        () => w1Count = -1;
    }
    return w1StopError;
}
