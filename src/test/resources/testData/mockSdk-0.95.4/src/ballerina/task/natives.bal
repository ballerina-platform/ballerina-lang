package ballerina.task;

@Description { value:"Schedules a timer task"}
@Param { value:"onTrigger: The function which gets called when the timer goes off" }
@Param { value:"onError: The function that gets called if the onTrigger function returns an error" }
@Param { value:"schedule: Specifies the initial delay and interval of the timer task" }
@Return { value:"The unique ID of the timer task that was scheduled" }
@Return { value:"This error will be returned if an occurs while scheduling the timer task" }
public native function scheduleTimer (
  function() returns (error) onTrigger,
  function(error e) onError,
  struct {
    int delay = 0;
    int interval;
  } schedule) returns (string taskId, error e);

@Description { value:"Schedules an appointment task"}
@Param { value:"onTrigger: The function which gets called when the appointment falls due" }
@Param { value:"onError: The function that gets called if the onTrigger function returns an error" }
@Param { value:"scheduleCronExpression: Specifies the Cron expression of the schedule" }
@Return { value:"The unique ID of the appointment task that was scheduled" }
@Return { value:"This error will be returned if an occurs while scheduling the appointment task" }
public native function scheduleAppointment (
       function() returns (error) onTrigger,
       function(error e) onError,
       string scheduleCronExpression) returns (string taskId, error e);

@Description { value:"Stops the timer task with ID taskID"}
@Param { value:"taskID: The unique ID of the timer task that has to be stopped" }
@Return { value:"This error will be returned if an error occurs while stopping the task" }
public native function stopTask (string taskID) returns (error);