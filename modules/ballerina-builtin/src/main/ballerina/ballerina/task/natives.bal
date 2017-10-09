package ballerina.task;

import ballerina.doc;

public struct TimerScheduler{
    int delay = 0;
    int interval;
}

public struct AppointmentScheduler{
    int minute = -1;
    int hour = -1;
    int dayOfWeek = -1;
    int dayOfMonth = -1;
    int month = -1;
}

@doc:Description { value:"Schedules the task service with delay and interval"}
@doc:Param { value:"onTrigger: This is the function which is executed while scheduling the task" }
@doc:Param { value:"onError: This is the function which is executed in case of failure in scheduling the task" }
@doc:Param { value:"schedule: It is a struct. Which contains the delay and interval" }
@doc:Return { value:"int: The identifier of the scheduled task" }
@doc:Return { value:"any: The error which is occurred while scheduling the task" }
public native function scheduleTimer (
  any onTrigger,
  any onError,
  TimerScheduler timerScheduler) (int, any);

@doc:Description { value:"Schedules the task service with cron expression"}
@doc:Param { value:"onTrigger: This is the function which is executed while scheduling the task" }
@doc:Param { value:"onError: This is the function which is executed in case of failure in scheduling the task" }
@doc:Param { value:"schedule: It is a struct. Which contains the delay and interval" }
@doc:Return { value:"int: The identifier of the scheduled task" }
@doc:Return { value:"any: The error which is occurred while scheduling the task" }
public native function scheduleAppointment (
  any onTrigger,
  any onError,
  AppointmentScheduler appointmentScheduler) returns (int, any);

@doc:Description { value:"Stops the task service which corresponds to the task identifier"}
@doc:Param { value:"taskID: The identifier of the scheduled task" }
@doc:Return { value:"any: The error which is occurred while stopping the task" }
public native function stopTask (int taskID) returns (any);