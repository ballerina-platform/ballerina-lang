## Module Overview

This module provides the functionality to configure and manage Task Listeners and Task Schedulers.
Task Listeners and Task Schedulers can be used to perform tasks periodically.

#### Task Listeners

A Task `Listener` can be used to create a service listener, which will be triggered at specified times.

Below are the two types of configurations that can be used to configure a Task Listener, either as a timer or as an appointment.

- `TimerConfiguration`
- `AppointmentConfiguration`

##### Task Listener as a Timer

The `TimerConfiguration` can be used to configure a task that needs to be executed periodically.

The following code snippet shows how to create a listener, which registers a task with an initial delay of 3000 milliseconds and is executed every 1000 milliseconds for 10 times.

```ballerina
task:TimerConfiguration timerConfiguration = {
    intervalInMillis: 1000,
    initialDelayInMillis: 3000,
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 10
};

listener task:Listener timer = new(timerConfiguration);

// Creating a service on the `timer` task Listener.
service timerService on timer {
    // This resource triggers when the timer goes off.
    resource function onTrigger() {
    }
}
```

For an example on the usage of the `task:Listener` as a timer, see the [Task Service Timer Example](https://ballerina.io/learn/by-example/task-service-timer.html).

##### Task Listener as an Appointment

The `AppointmentConfiguration` can be used to schedule an appointment.
  
The following code snippet shows how to create a task appointment, which registers a service using a CRON expression to execute the task every second for 10 times.

```ballerina
task:AppointmentConfiguration appointmentConfiguration = {
    // This cron expression will schedule the appointment once every second.
    appointmentDetails: "* * * * * ?",
    // Number of recurrences will limit the number of times the timer runs.
    noOfRecurrences: 10
};

listener task:Listener appointment = new(appointmentConfiguration);

// Creating a service on the `appointment` task Listener.
service appointmentService on appointment {
    // This resource triggers when the appointment is due.
    resource function onTrigger() {
    }
}
```

For an example on the usage of the `task:Listener` as an appointment, see the [Task Service Appointment Example](https://ballerina.io/learn/by-example/task-service-appointment.html).

#### Task Schedulers

A Task `Scheduler` can be used to create timers/appointments dynamically. Service(s) can be attached to the `Scheduler` so that they can be invoked when the Scheduler is triggered. 

Similar to Task Listeners, below are the two types of configurations that can be used to configure a Task Scheduler either as a timer or as an appointment.

- `TimerConfiguration`
- `AppointmentConfiguration`

##### Task Scheduler as a Timer

A `Scheduler` can be used to create timers via its `TimerConfiguration`.

The following code snippet shows how to create a `task:Scheduler` as a timer.

```ballerina
task:TimerConfiguration timerConfiguration = {
        intervalInMillis: 1000,
        initialDelayInMillis: 0,
        noOfRecurrences: 10
};
task:Scheduler timer = new(timerConfiguration);
```

For an example on the usage of the `task:Scheduler` as a timer, see the [Task Scheduler Timer Example](https://ballerina.io/learn/by-example/task-scheduler-timer.html).

#### Task Scheduler as an Appointment

A `Scheduler` can also be used to create appointments via its `AppointmentConfiguration`. 

The following code snippet shows how to create a Task Scheduler as an appointment.

```ballerina
task:AppointmentConfiguration appointmentConfiguration = {
        appointmentDetails: "* * * * * ?",
        noOfRecurrences: 10
};
task:Scheduler appointment = new(appointmentConfiguration);
```

For an example on the usage of the `task:Scheduler` as an appointment, see the [Task Scheduler Appointment Example](https://ballerina.io/learn/by-example/task-scheduler-appointment.html).
