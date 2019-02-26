import ballerina/log;
import ballerina/task;

// Task Timer configuration record to configura task listener.
task:TimerConfiguration timerConfiguration = {
    interval: 1000,
    initialDelay: 3000,
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 10
};

// Initialize the listener using pre defined configurations.
listener task:Listener timer = new(timerConfiguration);

int count = 0;

// Creating a service on the task Listener.
service timerService on timer {
    // This resource triggers when the timer goes off.
    resource function onTrigger() returns error? {
        count = count + 1;
        log:printInfo("Cleaning up...");
        log:printInfo(string.convert(count));

        // Returning an error to show the usage when an error is returned
        // from onTrigger() resource.
        if (count == 5) {
            error e = error("Count error.");
            return e;
        }
    }

    // This resource will trigger when an error is returned from the
    // onTrigger() resource.
    resource function onError(error e) {
        log:printError("Cleanup failed", err = e);
    }
}
