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

# Schedules an appointment.
public type Appointment object {
    // The function which gets called when the appointment is up
    private (function () returns error?) onTrigger;
    // The function that gets called if the onTrigger function returns an error
    private (function(error) returns ())? onError;
    // Specifies the Cron expression of the schedule
    private string scheduleCronExpression;
    // Unique task ID which will be used when this appointment is cancelled
    private string taskId;
    // Keeps track whether the appointment is scheduled to ensure that a scheduled appointment cannot be
    // appointment again unless it is cancelled
    private boolean isRunning;

    public new(onTrigger, onError, scheduleCronExpression) {}

    // Schedule the appointment
    public extern function schedule();
    // Cancel the appointment
    public extern function cancel();
};
