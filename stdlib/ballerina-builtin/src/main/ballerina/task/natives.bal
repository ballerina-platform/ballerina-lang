// Copyright (c) 2017 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
//
// WSO2 Inc. licenses this file to you under the Apache License,
// Version 2.0 (the "License"); you may not use this file except
// in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package ballerina.task;

@Description {value:"Schedules a timer task"}
@Param {value:"onTrigger: The function which gets called when the timer goes off"}
@Param {value:"onError: The function that gets called if the onTrigger function returns an error"}
@Param {value:"schedule: Specifies the initial delay and interval of the timer task"}
@Return {value:"The unique ID of the timer task that was scheduled"}
@Return {value:"This error will be returned if an occurs while scheduling the timer task"}
public native function scheduleTimer ((function() returns error?) onTrigger,
                                      (function(error) returns ())? onError,
                                      {int delay = 0; int interval;} schedule) returns string;

@Description {value:"Schedules an appointment task"}
@Param {value:"onTrigger: The function which gets called when the appointment falls due"}
@Param {value:"onError: The function that gets called if the onTrigger function returns an error"}
@Param {value:"scheduleCronExpression: Specifies the Cron expression of the schedule"}
@Return {value:"The unique ID of the appointment task that was scheduled"}
@Return {value:"This error will be returned if an occurs while scheduling the appointment task"}
public native function scheduleAppointment ((function () returns error?) onTrigger,
                                            (function(error) returns ())? onError,
                                            string scheduleCronExpression) returns string;

@Description {value:"Stops the timer task with ID taskID"}
@Param {value:"taskID: The unique ID of the timer task that has to be stopped"}
@Return {value:"This error will be returned if an error occurs while stopping the task"}
public native function stopTask (string taskID) returns error?;
