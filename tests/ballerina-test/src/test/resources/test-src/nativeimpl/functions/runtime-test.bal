import ballerina/runtime;
import ballerina/time;

function testSleepCurrentThread () {
    runtime:sleepCurrentWorker(1000);
}

function testConcurrentSleep () returns (int[]) {
    worker w1 {
        int[] result = [];
        int start = time:currentTime().time;
        runtime:sleepCurrentWorker(1000);
        int end = time:currentTime().time;
        result[0] = end - start;
        result -> w2;
        result <- w5;
        return result;
    }
    worker w2 {
        int start = time:currentTime().time;
        runtime:sleepCurrentWorker(1000);
        int end = time:currentTime().time;
        int[] result;
        result <- w1;
        result[1] = end - start;
        result -> w3;
    }
    worker w3 {
        int start = time:currentTime().time;
        runtime:sleepCurrentWorker(2000);
        int end = time:currentTime().time;
        int[] result;
        result <- w2;
        result[2] = end - start;
        result -> w4;
    }
    worker w4 {
        int start = time:currentTime().time;
        runtime:sleepCurrentWorker(2000);
        int end = time:currentTime().time;
        int[] result;
        result <- w3;
        result[3] = end - start;
        result -> w5;
    }
    worker w5 {
        int start = time:currentTime().time;
        runtime:sleepCurrentWorker(1000);
        int end = time:currentTime().time;
        int[] result;
        result <- w4;
        result[4] = end - start;
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
