import ballerina/io;
import ballerina/task;

int w1Count;
error errorW1;
string errorMsgW1;
task:Appointment? app;

function scheduleAppointment(string cronExpression, string errMsgW1) {
    worker default {
        errorMsgW1 = errMsgW1;
        app <- w1;
    }
    worker w1 {
        (function() returns error?) onTriggerFunction = onTriggerW1;
        task:Appointment? appW1;
        if (errMsgW1 == "") {
            appW1 = new task:Appointment(onTriggerFunction, (), cronExpression);
            _ = appW1.schedule();
        } else {
            function(error) onErrorFunction = onErrorW1;
            appW1 = new task:Appointment(onTriggerFunction, onErrorFunction, cronExpression);
            _ = appW1.schedule();
        }
        appW1 -> default;
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
    return ();
}

function onErrorW1(error e) {
    io:println("w1:onErrorW1");
    errorW1 = e;
}

function getCount() returns (int) {
    return w1Count;
}

function getError() returns (string) {
    string w1ErrMsg;
    if (errorW1 != null) {
        w1ErrMsg = errorW1.message;
    }
    return w1ErrMsg;
}

function cancelAppointment() {
    _ = app.cancel();
    w1Count = -1;
}
