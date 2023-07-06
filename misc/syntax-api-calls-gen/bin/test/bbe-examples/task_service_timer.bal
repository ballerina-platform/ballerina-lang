import ballerina/io;
import ballerina/task;

// The [`task:TimerConfiguration`](https://ballerina.io/swan-lake/learn/api-docs/ballerina/task/records/TimerConfiguration.html) record to configure the task listener.
task:TimerConfiguration timerConfiguration = {
    intervalInMillis: 1000,
    initialDelayInMillis: 3000,
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 10
};

// Initializes the listener using the configurations defined above.
listener task:Listener timer = new (timerConfiguration);

int count = 0;

// Creating a service bound to the task listener.
service timerService on timer {
    // This resource triggers when the timer goes off.
    resource function onTrigger() {
        count += 1;
        io:println("MyCounter: ", count);
    }

}
