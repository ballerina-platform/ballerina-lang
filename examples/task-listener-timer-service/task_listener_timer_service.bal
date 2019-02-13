import ballerina/io;
import ballerina/task;

task:TimerConfiguration timerConfiguration = {
    interval: 1000,
    delay: 3000,
    noOfRecurrences: 10
};

listener task:Listener timer = new(timerConfiguration);

int count = 0;

service timerService on timer {
    resource function onTrigger() returns error? {
        count = count + 1;
        io:println("Cleaning up...");
        io:println(count);

        if (count == 5) {
            error e = error("Cleanup error");
            return e;
        }
    }

    resource function onError(error e) {
        io:print("[ERROR] cleanup failed: ");
        io:println(e);
    }
}
