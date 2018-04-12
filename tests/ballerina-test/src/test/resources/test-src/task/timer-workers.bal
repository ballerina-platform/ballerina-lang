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
        task:Timer? t1;
        task:Timer? t2;
        errorMsgW1 = errMsgW1;
        errorMsgW2 = errMsgW2;
        t1 <- w1;
        t2 <- w2;

        timer1 = t1;
        timer2 = t2;
    }
    worker w1 {
        task:Timer? t;
        (function() returns error?) onTriggerFunction = onTriggerW1;
        if (errMsgW1 == "") {
            task:Timer t1 = new(onTriggerFunction, (), w1Interval, delay = w1Delay);
            t1.start();
            t = t1;
        } else {
            function (error) onErrorFunction = onErrorW1;
            task:Timer t1 = new(onTriggerFunction, onErrorFunction, w1Interval, delay = w1Delay);
            t1.start();
            t = t1;
        }
        t -> default;
    }
    worker w2 {
        task:Timer? t;
        (function() returns error?) onTriggerFunction = onTriggerW2;
        if (errMsgW2 == "") {
            task:Timer t2 = new(onTriggerFunction, (), w2Interval, delay = w2Delay);
            t2.start();
            t = t2;
        } else {
            function (error) onErrorFunction = onErrorW2;
            task:Timer t2 = new(onTriggerFunction, onErrorFunction, w2Interval, delay = w2Delay);
            t2.start();
            t = t2;
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

    //error? w1StopError = task:stopTask(w1TaskId);
    //match w1StopError {
    //    error err => {}
    //    () => w1Count = -1;
    //}
    //error? w2StopError = task:stopTask(w2TaskId);
    //match w2StopError {
    //    error err => {}
    //    () => w2Count = -1;
    //}
    //return (w1StopError, w2StopError);
}
