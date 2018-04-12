import ballerina/runtime;
import ballerina/time;

function testSleepCurrentThread () {
    runtime:sleepCurrentWorker(1000);
}

function testConcurrentSleep () returns (int[]) {
    worker w1 {
        int[] result = [];
        int startTime = time:currentTime().time;
        runtime:sleepCurrentWorker(1000);
        int end = time:currentTime().time;
        result[0] = end - startTime;
        result -> w2;
        result <- w5;
        return result;
    }
    worker w2 {
        int startTime = time:currentTime().time;
        runtime:sleepCurrentWorker(1000);
        int end = time:currentTime().time;
        int[] result;
        result <- w1;
        result[1] = end - startTime;
        result -> w3;
    }
    worker w3 {
        int startTime = time:currentTime().time;
        runtime:sleepCurrentWorker(2000);
        int end = time:currentTime().time;
        int[] result;
        result <- w2;
        result[2] = end - startTime;
        result -> w4;
    }
    worker w4 {
        int startTime = time:currentTime().time;
        runtime:sleepCurrentWorker(2000);
        int end = time:currentTime().time;
        int[] result;
        result <- w3;
        result[3] = end - startTime;
        result -> w5;
    }
    worker w5 {
        int startTime = time:currentTime().time;
        runtime:sleepCurrentWorker(1000);
        int end = time:currentTime().time;
        int[] result;
        result <- w4;
        result[4] = end - startTime;
        result -> w1;
    }
}

function testSetProperty (string name, string value) {
    runtime:setProperty(name, value);
}

function testGetProperty (string name) returns (string) {
    return runtime:getProperty(name);
}

function testGetProperties () returns (map) {
    return runtime:getProperties();
}

function testGetCurrentDirectory () returns (string) {
    return runtime:getCurrentDirectory();
}

function testGetFileEncoding () returns (string) {
    return runtime:getFileEncoding();
}
