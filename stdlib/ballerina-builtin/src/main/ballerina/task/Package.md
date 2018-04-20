## Package overview

This package includes functions to manage task timers and task appointments.

### Task timers

Timers execute periodic tasks. The initial execution of a task happens after a specific time period from the task registration, which is denoted using `delay`.  The frequency at which the tasks needs to be executed is defined using the `interval`. If the `delay` is not specified the `delay` will be the same as `interval`. The `delay` and `interval` times are defined in milliseconds.

The tasks that need to be executed is defined in the `onTriggerFunction` function.  If an error is returned when executing the `onTriggerFunction` function, the `onErrorFunction` is executed.

The example given below defines the `doTask` function as the  `onTriggerFunction` function. It is executed a second after the task registers and runs every 0.5 seconds. If the function returns an error, the  `onError` function is executed. This function is responsible for handling errors that takes place while doing the specified task.

```ballerina

    (function() returns error?) onTriggerFunction = doTask;
    (function(error)) onErrorFunction = onError;
    timer = new task:Timer(onTriggerFunction, onErrorFunction, 500, delay = 1000);
    _ = timer.start();

```

### Task appointments

A task appointment is similar to a real-world appointment. The task appointment is configured to run at a given time pattern. A cron expression is used to define the time, and the frequency a task appointment needs to run. 

The `onTriggerFunction` function of the task is called when the appointment is due.  If an error is returned when executing the `onTriggerFunction` function, the `onErrorFunction` is called.

The example given below triggers the `onTrigger` function every 5 seconds. If an error is returned, the `cleanupError` function is called.

```ballerina
    (function() returns error?) onTriggerFunction = onTrigger;
    (function (error)) onErrorFunction = cleanupError;
    app = new task:Appointment(onTriggerFunction, onErrorFunction, "0/05 * * * * ?");
    _ = app.schedule();
```

## Samples

### Tasks timer

In this sample, a task is registered with a delay of 1000 milliseconds and is made to run every 1000 milliseconds. The `onTrigger ` function is triggered when the clock goes off. The `onError` function is executed if an error is returned from the `onTrigger` function. Further, the count variable is incremented by the task and if the count is equal to 10, an error is returned. If the count is equal to 20, the task is stopped using the `stopTask()` function.

```ballerina
import ballerina/task;
import ballerina/io;
import ballerina/runtime;

int count;
task:Timer? timer;

function main(string... args) {
    io:println("tasks sample is running");
    scheduleTimer(1000,1000);
    // Keep the program running for 100*1000 milliseconds.
    runtime:sleepCurrentWorker(100*1000);
}

function scheduleTimer(int delay, int interval) {
    // Point to the trigger function.
    (function() returns error?) onTriggerFunction = onTrigger;
    // Point to the error function.
    (function (error)) onErrorFunction = onError;
    // Register a task with given ‘onTrigger’ and ‘onError’ functions, and with given ‘delay’ and ‘interval’ times. 
    timer = new task:Timer(onTriggerFunction, onErrorFunction, interval, delay = delay);
    // Start the timer.
    _ = timer.start();
}

// Define the ‘onError’ function for the task timer.
function onError(error e) {
    io:print("[ERROR] failed to execute timed task");
    io:println(e);
}

// Define the ‘onTrigger’ function for the task timer.
function onTrigger() returns error? {
    count = count + 1;
    if(count == 10) {
        error e = {message:"Task cannot be performed when the count is 10"};
	    //The ‘onError’ function is called when the error is returned.
        return e;
    }

    if(count == 20) {
        _ = stopTask();
    }
    io:println("on trigger : count value is: " + count);
    return ();
}

// Define the function to stop the task.
function stopTask() returns error? {
    io:println("Stopping task");
    _ = timer.stop();
    count = -1;
    return ();
}

```

### Tasks appointment

In this sample, a task appointment is registered with a cron expression to run every 5 seconds. Therefore, the `onTrigger ` function is triggered every 5 seconds. The `onError` function is executed if an error is returned from the `onTrigger` function. Further, the count variable is incremented by the task and if the count is equal to 10, an error is returned. If the count is equal to 20, the task is stopped.


```ballerina
import ballerina/task;
import ballerina/io;
import ballerina/runtime;

int count;
task:Appointment? app;

function main(string... args) {
    io:println("tasks sample is running");
    // To schedule the appointment with given cron expression.
    scheduleAppointment("0/05 * * * * ?");
    // Keep the program running for 100*1000 seconds
    runtime:sleepCurrentWorker(100*1000);
}
function scheduleAppointment(string cronExpression) {
    // Define on trigger function
    (function() returns error?) onTriggerFunction = onTrigger;
    // Define on error function
    (function (error)) onErrorFunction = onError;
    // Schedule appointment.
    app = new task:Appointment(onTriggerFunction, onErrorFunction, cronExpression);
    _ = app.schedule();
}

function onTrigger() returns error? {
    io:println("tasks is triggered and the value of count is : " + count);
    count = count + 1;

    if(count == 10) {
        error e = {message:"Task appointment cannot be executed when the count is 10"};
        // The ‘onError’ function is called when the error is returned.
        return e;
    }

    if(count == 20) {
        cancelAppointment();
        io:println("appointment cancelled");
    }
    return ();
}

// Define the ‘onError’ function for the task timer.
function onError(error e) {
    io:print("[ERROR] failed to execute timed task");
    io:println(e);
}

// Define the function to stop the task.
function cancelAppointment() {
    _ = app.cancel();
    count = -1;
}

```
## Package content
