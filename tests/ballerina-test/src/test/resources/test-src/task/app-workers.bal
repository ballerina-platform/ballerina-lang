import ballerina.io;
import ballerina.task;

int w1Count;
error errorW1;
string errorMsgW1;

function scheduleAppointment (string cronExpression, string errMsgW1) returns (string w1TaskId) {
    worker default {
        errorMsgW1 = errMsgW1;
        w1TaskId <- w1;
        return;
    }
    worker w1 {
        function () returns (error) onTriggerFunction = onTriggerW1;
        string w1TaskIdX;
        if(errMsgW1 == ""){
            w1TaskIdX, _= task:scheduleAppointment(onTriggerFunction, null, cronExpression);
        } else {
            function (error) onErrorFunction = onErrorW1;
            w1TaskIdX, _ = task:scheduleAppointment(onTriggerFunction, onErrorFunction, cronExpression);
        }
        w1TaskIdX -> default;
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

function getCount () returns (int) {
    return w1Count;
}

function getError () returns (string w1ErrMsg) {
    if(errorW1 != null) {
        w1ErrMsg = errorW1.message;
    }
    return;
}

function stopTask (string w1TaskId) returns (error w1StopError) {
    w1StopError = task:stopTask(w1TaskId);
    if(w1StopError == null) {
        w1Count = -1;
    }
    return;
}
