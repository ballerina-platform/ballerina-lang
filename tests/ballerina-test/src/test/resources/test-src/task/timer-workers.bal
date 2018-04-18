import ballerina/task;
import ballerina/io;

int w1Count;
int w2Count;
error errorW1;
error errorW2;
string errorMsgW1;
string errorMsgW2;

task:Timer? timer1;
task:Timer? timer2;

function scheduleTimer(int w1Delay, int w1Interval, int w2Delay, int w2Interval, string errMsgW1, string errMsgW2) {
    worker default {
        errorMsgW1 = errMsgW1;
        errorMsgW2 = errMsgW2;
        timer1 <- w1;
        timer2 <- w2;
    }
    worker w1 {
        task:Timer? t;
        (function() returns error?) onTriggerFunction = onTriggerW1;
        if (errMsgW1 == "") {
            t = new task:Timer(onTriggerFunction, (), w1Interval, delay = w1Delay);
            _ = t.start();
        } else {
            function (error) onErrorFunction = onErrorW1;
            t = new task:Timer(onTriggerFunction, onErrorFunction, w1Interval, delay = w1Delay);
            _ = t.start();
        }
        t -> default;
    }
    worker w2 {
        task:Timer? t;
        (function() returns error?) onTriggerFunction = onTriggerW2;
        if (errMsgW2 == "") {
            t = new task:Timer(onTriggerFunction, (), w2Interval, delay = w2Delay);
            _ = t.start();
        } else {
            function (error) onErrorFunction = onErrorW2;
            t = new task:Timer(onTriggerFunction, onErrorFunction, w2Interval, delay = w2Delay);
            _ = t.start();
        }
        t -> default;
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

function stopTasks() {
    _ = timer1.stop();
    _ = timer2.stop();
    w1Count = -1;
    w2Count = -1;
}
