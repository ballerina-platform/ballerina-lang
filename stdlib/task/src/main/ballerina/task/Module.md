## Module overview

Task Listeners and Task Schedulers can be used to perform tasks periodically. This module provides the functionality to configure and manage Task Listeners and Task Schedulers.

- [Task Listeners](#task-listeners)
    - [The Timer configuration for a Listener](#The-Timer-configuration-for-a-Listener)
    - [The Appointment configuration for a Listener](#The-Appointment-configuration-for-a-Listener)
- [Task Schedulers](#Task-Schedulers)
    - [The Timer configuration for a Scheduler](#The-Timer-configuration-for-a-Scheduler)
    - [The Appointment configuration for a Scheduler](#The-Appointment-configuration-for-a-Scheduler)

### Task Listeners

A Task `Listener` can be used to create a service listener, which will be triggered at specified times.

Below are the two types of configurations that can be used to configure a Task Listener, either as a timer or as an appointment.

- `TimerConfiguration`
- `AppointmentConfiguration`

#### The Timer configuration for a Listener 

The `TimerConfiguration` can be used to configure a task that needs to be executed periodically. The `TimerConfiguration` consists of three fields out of which two are optional.

- `interval` - The time interval to wait for the listener to get triggered. This should be given in milliseconds.
- `initialDelay` - The initial delay before the task gets triggered first. If this is set to `0`, the task will execute immediately. If the field is not set, the `interval` will be applied as the initial delay by default. This is optional and should be given in milliseconds.
- `noOfRecurrences` - The number of times a particular task needs to be executed. This is optional and should be given as an `int`.

The following example creates a listener, which registers a task with an initial delay of 3000 milliseconds and is executed every 1000 milliseconds for 10 times. The `onTrigger ` resource function is triggered when the clock goes off. The `count` variable is incremented by the task.

```ballerina
import ballerina/log;
import ballerina/task;

// Task Timer configuration record to configure a Task Listener.
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

#### The Appointment configuration for a Listener

The `AppointmentConfiguration` can be used to schedule an appointment. This Listener configuration consists of two main fields.

  - `appointmentDetails` - Appointment details is a union of `task:AppointmentData` and `string`. `AppointmentDetails` can be given as either a CRON Expression (as a `string`), or an `AppointmentData` record type. An `AppointmentData` record includes seven fields to provide the appointment details.
  - `noOfRecurrences` - The number of times a particular task needs to be executed. This is optional and should be given as an `int`.
  
The following example creates a task appointment, which registers a service using a CRON expression to execute the task every 5 seconds for 11 times. The `count` variable is incremented by the task.

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

### Task Schedulers

A Task `Scheduler` can be used to create timers/appointments dynamically. Service(s) can be attached to the `Scheduler`, so that they can be invoked when the Scheduler is triggered. 

A `Scheduler` consists of the following APIs.

- `start()` - Starts the Task Scheduler and executes the services attached to it.
- `stop()` - Stops the task. This will shutdown all the processes scheduled on the Scheduler.
- `pause()` - Pauses the Scheduler. This will temporarily halt the execution of the task.
- `resume()` - Resumes a task, which has been paused.
- `attach()` - Attaches a service to the Scheduler. An optional `attachment` parameter can be passed to the function so that it will propagate into the resource.
- `detach()` - Detaches any service(s) that is/are attached to the task.

Similar to Task Listeners, below are the two types of configurations that an be used to configure a Task Scheduler, either as a timer or as an appointment.

- `TimerConfiguration`
- `AppointmentConfiguration`

>**Info:** The configurations are similar to the [`Task Listener`](#task-listeners).

#### The Timer configuration for a Scheduler

A `Scheduler` can be used to create timers via its `TimerConfiguration`.

The following example creates a `task:Scheduler` as a timer. The `createTimer()` function uses its input values to create a Task Scheduler dynamically. It attaches the `timerService` to the `timer` Scheduler it created. Calling the `timer.start()` function starts the `timer` Scheduler.

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

#### The Appointment configuration for a Scheduler

A `Scheduler` can also be used to create appointments via its `AppointmentConfiguration`. 

The following example creates a Task Scheduler as an appointment. The `createAppointment()` function creates an appointment using the CRON expression provided as the input parameter. A service can be attached to the Scheduler using the `attach()` function. Calling the `appointment.start()` function starts the `appointment` Scheduler.

```ballerina
public function createAppointment(string cronExpression, int recurrences) {
    task:AppointmentConfiguration appointmentConfiguration = {
            appointmentDetails: cronExpression,
            noOfRecurrences: recurrences
    };
    task:Scheduler appointment = new(appointmentConfiguration);
    
    var result  = appointment.attach(appointmentService);
    if (result is error) {
        log:printError("Error attaching service: ", err = result);
        return;
    }
    result = appointment.start();
    if (result is error) {
        log:printError("Error attaching service: ", err = result);
        return;
    }
}

service appointmentService = service {
    resource function onTrigger() {
        // Task to run when the appointment triggers.
    }
};
```
