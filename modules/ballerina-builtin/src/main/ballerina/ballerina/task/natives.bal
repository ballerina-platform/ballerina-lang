package ballerina.task;

import ballerina.doc;

@doc:Description { value:"Schedules the task service with delay and interval"}
@doc:Param { value:"onTrigger: This is the function which is executed while scheduling the task" }
@doc:Param { value:"onError: This is the function which is executed in case of failure in scheduling the task" }
@doc:Param { value:"schedule: It is a struct. Which contains the delay and interval" }
@doc:Return { value:"string: The identifier of the scheduled task" }
@doc:Return { value:"error: The error which is occurred while scheduling the task" }
public native function scheduleTimer (
  any onTrigger,
  any onError,
  struct {
    int delay = 0;
    int interval;
  } timerScheduler) returns (string taskId, error e);

@doc:Description { value:"Stops the task service which corresponds to the task identifier"}
@doc:Param { value:"taskID: The identifier of the scheduled task" }
@doc:Return { value:"error: The error which is occurred while stopping the task" }
public native function stopTask (string taskID) returns (error);