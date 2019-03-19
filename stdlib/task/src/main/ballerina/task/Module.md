## Module overview

This module provides functionality to configure and manage Task Listeners and Task Schedulers, which will be executed periodically.

### Task Listeners

Task `Listener` can be used to create a service listener, which will trigger on specified times. Listener can be configured using listener configurations.
There are two types of configurations for a Listener.
- `TimerConfiguration`
- `AppointmentConfiguration`

If a task is needed to be run periodically, `TimerConfiguration` can be used. `TimerConfiguration` consists of three fields, from which two are optional.
- `interval` - Timer interval by which the listener should trigger. This should be given in Milliseconds.
- `initialDelay` - [Optional] Initial delay before the task should trigger. If this is set to `0`, task will run immediately. If the field is not set, interval will be taken as the initial delay. This should be given in Milliseconds.
- `noOfRecurrences` - [Optional] If there's a requirement to run a particular task only for a number of times, this field can be used. This should be given as an int.

The following code snippet creates a listener which have an initial delay of 5000 Milliseconds (5 seconds) and with an interval of 1000 Milliseconds (1 second). Then we can create a service on the listener. The service consists of the resource function onTrigger() which will be executed when the timer goes off.

```ballerina
    listener task:Listener timer = new({
        interval: 1000,
        delay: 5000,
        noOfRecurrences: 3
    });
    service timerService on timer {
        resource function onTrigger() {
            io:println("Triggering");
        }
    }
```

`Listener` can be used to schedule an appointment, like the appointments we see in real world. Listener configuration has two main fields.
  - `appointmentDetails`
  - `noOfRecurrences`
  
Appointment details is again a union of `task:AppointmentData` and `string`. `AppointmentDetails` can be given as either a `cronExpression` as a `string`, or an `AppointmentData` record type. `AppointmentData` record includes seven fields to provide the appointment details.
  
Following code snippet shows how to create an Appointment using `task:Listener`. It will trigger the `onTrigger()` resource by every two seconds, starting from the 0<sup>th</sup> seconds of the minute.

```ballerina
    listener task:Listener appointment = new({
        appointmentDetails: "0/2 * * * * ?"
    });
    service appointmentService on appointment {
        resource function onTrigger() {
            io:println("Triggering");
        }
    }
```

### Task Scheduler

A task `Scheduler` can be used to create timers / appointments dynamically. Service(s) can be attached to the `Scheduler`, so that they can be invoked when the Scheduler goes off. Scheduler can be created as a timer or an appointment. The configurations are as same as the `Listener`.

Code snippet for an example timer `task:Scheduler`. 

```ballerina
public function createTimer(int interval, int delay, int recurrences) {
    task:TimerConfiguration timerConfiguration = {
            interval: interval,
            delay: delay,
            noOfRecurrences: recurrences
    };
    task:Scheduler timer = new(timerConfiguration);
    
    var result  = timer.attach(timerService);
    if (result is error) {
        log:printError("Error attaching service: ", err = result);
        return;
    }
    result = timer.start();
    if (result is error) {
        log:printError("Error attaching service: ", err = result);
        return;
    }
}

service timerService = service {
    resource function onTrigger() {
        // Task to run when the timer triggers.
    }
};
```

`Scheduler` can also be used to create appointments, by providing the configuration, `

`Scheduler` has following APIs.

- `start()` - Starts the task scheduler, and run the services attached to it.
- `stop()` - Stops the task. This will shutdown all the processes scheduled on the scheduler.
- `pause()` - Pauses the scheduler. This will temporarily halts the task execution.
- `resume()` - Resumes a task, which has been paused.
- `attach()` - Attaches a service to the scheduler. An optional parameter, `attachment` can be passed to the function, so that it will propagate into the resource.
- `detach()` - Detaches any attached services from the task.

## Samples

### Task Listener - Timer

In this sample, a task is registered with a delay of 1000 milliseconds and is made to run every 1000 milliseconds. 
The `onTrigger ` resource function is triggered when the clock goes off. 
The count variable is incremented by the task.

```ballerina
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
    resource function onTrigger() {
        count = count + 1;
        log:printInfo("Cleaning up...");
        log:printInfo(string.convert(count));
    }
}
```

### Task Listener - Appointment

In this sample, a task appointment is registered with a CRON expression, to run the task every 5 seconds. Therefore, the `onTrigger ` function is triggered every 5 seconds. The count variable is incremented by the task.


```ballerina
import ballerina/log;
import ballerina/task;

// Task Appointment configuration record to task Listener.
// Task Appointment can have either a a cronExpression (`string`)
// or a `AppointmentData` record the `appointmentData` field.
// Optionally a `noOfRecurrences` can be provided to limit the number of runs
// an appointment should run.
task:AppointmentConfiguration appointmentConfiguration = {
    // This cron expression will schedule the appointment once every 2 seconds.
    appointmentDetails: "0/2 * * * * ?",
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 11
};

// Initialize the listener using pre defined configurations.
listener task:Listener appointment = new(appointmentConfiguration);

int count = 0;

// Creating a service on the task Listener.
service appointmentService on appointment {
    // This resource triggers when the appointment is due.
    resource function onTrigger() {
        count = count + 1;
        log:printInfo("Cleaning up...");
        log:printInfo(string.convert(count));
    }
}
```
