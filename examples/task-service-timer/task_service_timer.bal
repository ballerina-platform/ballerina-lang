import ballerina/log;
import ballerina/task;

// The Task Timer configuration record to configure the Task Listener.
task:TimerConfiguration timerConfiguration = {
    intervalInMillis: 1000,
    initialDelayInMillis: 3000,
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 10
};

// Initialize the listener using the above defined configurations.
listener task:Listener timer = new (timerConfiguration);

int count = 0;

// Creating a service on the task Listener.
service timerService on timer {
    // This resource triggers when the timer goes off.
    resource function onTrigger() {
        log:printInfo("Cleaning up...");
        log:printInfo(count.toString());
        count = count + 1;
    }
}
