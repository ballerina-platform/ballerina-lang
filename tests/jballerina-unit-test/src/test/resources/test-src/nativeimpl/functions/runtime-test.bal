import ballerina/runtime;
import ballerina/time;

function testSleep() {
    runtime:sleep(1000);
}

function testConcurrentSleep() returns (int[]) {
    worker w1 {
        int[] result = [];
        int startTime = time:currentTime().time;
        runtime:sleep(1000);
        int end = time:currentTime().time;
        result[0] = end - startTime;
        result -> w2;
        result = <- w5;
        result -> default;
    }
    worker w2 {
        int startTime = time:currentTime().time;
        runtime:sleep(1000);
        int end = time:currentTime().time;
        int[] result;
        result = <- w1;
        result[1] = end - startTime;
        result -> w3;
    }
    worker w3 {
        int startTime = time:currentTime().time;
        runtime:sleep(2000);
        int end = time:currentTime().time;
        int[] result;
        result = <- w2;
        result[2] = end - startTime;
        result -> w4;
    }
    worker w4 {
        int startTime = time:currentTime().time;
        runtime:sleep(2000);
        int end = time:currentTime().time;
        int[] result;
        result = <- w3;
        result[3] = end - startTime;
        result -> w5;
    }
    worker w5 {
        int startTime = time:currentTime().time;
        runtime:sleep(1000);
        int end = time:currentTime().time;
        int[] result;
        result = <- w4;
        result[4] = end - startTime;
        result -> w1;
    }

    int[] result = <- w1;
    return result;
}

function testGetProperty(string name) returns (string) {
    return runtime:getProperty(name);
}
