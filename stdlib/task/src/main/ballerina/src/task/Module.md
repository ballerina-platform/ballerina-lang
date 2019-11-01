## Module overview

This module provides the functionality to configure and manage Task Listeners and Task Schedulers.
Task Listeners and Task Schedulers can be used to perform tasks periodically.

### Examples

#### Task Listeners

A Task `Listener` can be used to create a service listener, which will be triggered at specified times.

Below are the two types of configurations that can be used to configure a Task Listener, either as a timer or as an appointment.

- `TimerConfiguration`
- `AppointmentConfiguration`

##### Task Listener as a Timer

The `TimerConfiguration` can be used to configure a task that needs to be executed periodically.

The following example creates a listener, which registers a task with an initial delay of 3000 milliseconds and is executed every 1000 milliseconds for 10 times. The `onTrigger ` resource function is triggered when the clock goes off. The `count` variable is incremented by the task.

```ballerina
import ballerina/log;
import ballerina/task;

// Task Timer configuration record to configure a Task Listener.
task:TimerConfiguration timerConfiguration = {
    intervalInMillis: 1000,
    initialDelayInMillis: 3000,
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
        log:printInfo("Cleaning up...");
        log:printInfo(count.toString());
        count = count + 1;
    }
}
```

##### Task Listener as an Appointment

The `AppointmentConfiguration` can be used to schedule an appointment.
  
The following example creates a task appointment, which registers a service using a CRON expression to execute the task every second for 10 times. The `count` variable is incremented by the task.

```ballerina
import ballerina/log;
import ballerina/task;

// Task Appointment configuration record to task Listener.
task:AppointmentConfiguration appointmentConfiguration = {
    // This cron expression will schedule the appointment once every second.
    appointmentDetails: "* * * * * ?",
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 10
};

// Initialize the listener using pre defined configurations.
listener task:Listener appointment = new(appointmentConfiguration);

int count = 0;

// Creating a service on the task Listener.
service appointmentService on appointment {
    // This resource triggers when the appointment is due.
    resource function onTrigger() {
        log:printInfo("Cleaning up...");
        log:printInfo(count.toString());
        count = count + 1;
    }
}
```

#### Task Schedulers

A Task `Scheduler` can be used to create timers/appointments dynamically. Service(s) can be attached to the `Scheduler`, so that they can be invoked when the Scheduler is triggered. 

Similar to Task Listeners, below are the two types of configurations that an be used to configure a Task Scheduler, either as a timer or as an appointment.

- `TimerConfiguration`
- `AppointmentConfiguration`

##### Task Scheduler as a Timer

A `Scheduler` can be used to create timers via its `TimerConfiguration`.

The following example creates a `task:Scheduler` as a timer. The `createTimer()` function uses its input values to create a Task Scheduler dynamically. It attaches the `timerService` to the `timer` Scheduler it created. Calling the `timer.start()` function starts the `timer` Scheduler.

```ballerina
import ballerina/io;
import ballerina/log;
import ballerina/task;

public function main() {
    createTimer(1000, 0, 10);
}

function createTimer(int interval, int delay, int recurrences) {
    task:TimerConfiguration timerConfiguration = {
            intervalInMillis: interval,
            initialDelayInMillis: delay,
            noOfRecurrences: recurrences
    };
    task:Scheduler timer = new(timerConfiguration);
    
    var result  = timer.attach(timerService);
    if (result is error) {
        log:printError("Error attaching service: ", result);
        return;
    }
    result = timer.start();
    if (result is error) {
        log:printError("Error attaching service: ", result);
        return;
    }
}

service timerService = service {
    resource function onTrigger() {
        io:println("Task Triggered");
    }
};
```

#### Task Scheduler as an Appointment

A `Scheduler` can also be used to create appointments via its `AppointmentConfiguration`. 

The following example creates a Task Scheduler as an appointment. The `createAppointment()` function creates an appointment using the CRON expression provided as the input parameter. A service can be attached to the Scheduler using the `attach()` function. Calling the `appointment.start()` function starts the `appointment` Scheduler.

```ballerina
import ballerina/io;
import ballerina/log;
import ballerina/task;

public function main() {
    createAppointment("* * * * * ?", 10);
}

function createAppointment(string cronExpression, int recurrences) {
    task:AppointmentConfiguration appointmentConfiguration = {
            appointmentDetails: cronExpression,
            noOfRecurrences: recurrences
    };
    task:Scheduler appointment = new(appointmentConfiguration);
    
    var result  = appointment.attach(appointmentService);
    if (result is error) {
        log:printError("Error attaching service: ", result);
        return;
    }
    result = appointment.start();
    if (result is error) {
        log:printError("Error attaching service: ", result);
        return;
    }
}

service appointmentService = service {
    resource function onTrigger() {
        io:println("Appointment Triggered");
    }
};
```
