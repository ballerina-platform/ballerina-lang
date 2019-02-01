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

# Configurations related to a Timer
#
# + interval - Timer interval, which triggers the onTrigger() resource.
# + delay - Delay after which the timer will run.
# + noOfRecurrings - Number of times to trigger the task, after which the task stops running.
public type TimerConfiguration record {
    int interval;
    int delay?;
    int noOfRecurrings?;
    !...;
};

# Configurations related to an Appointment
#
# + cronExpression - Cron expression for the appointment. Providing cronExpression will override all the other configurations.
# + seconds - Second(s) in a given minute, in which the appointment will run.
# + minutes - Minute(s) in a given hour, in which the appointment will run.
# + hours - Hour(s) in a given day, in which the appointment will run.
# + daysOfMonth - Day(s) of the month, in which the appointment will run.
# + months - Month(s) in a given year, in which the appointment will run.
# + daysOfWeek - Day(s) of a week, in which the appointment will run.
# + year - Year(s) in which the appointment will run.
# + noOfRecurrings - Number of times to trigger the task, after which the task stops running.
public type AppointmentConfiguration record {
    string cronExpression?;
    string seconds?;
    string minutes?;
    string hours?;
    string daysOfMonth?;
    string months?;
    string daysOfWeek?;
    string year?;
    int noOfRecurrings?;
    !...;
};

# Represents a ballerina task listener
public type Listener object {
    *AbstractListener;

    private TimerConfiguration|AppointmentConfiguration listenerConfiguration;
    private string taskId;
    private boolean isRunning;

    public function __init(TimerConfiguration|AppointmentConfiguration configs) {
        if (configs is TimerConfiguration) {
            if (configs["delay"] == ()) {
                configs.delay = configs.interval;
            }
        }
        self.listenerConfiguration = configs;
    }

    public function __attach(service s, map<any> annotationData) returns error? {
        return self.register(s, annotationData);
    }

    public function __start() returns error? {
        return self.start();
    }

    public function __stop() returns error? {
        return self.stop();
    }

    extern function register(service s, map<any> annotationData) returns error?;

    extern function stop() returns error?;

    # Attaches the provided service to the listener.
    #
    # + serviceToAttach - service which needs to be attached to the listener.
    # + return - Returns error if the process failed due to any reason, nil otherwise.
    public function attach(service serviceToAttach) returns error? {
        return self.register(serviceToAttach, {});
    }

    # Starts running the task. Task will not run until this has been called.
    #
    # + return - Returns error if the process failed due to any reason, nil otherwise.
    public extern function start() returns error?;

    # Cancels the listenr from running. This will stop, after finish running the existing jobs.
    #
    # + return - Returns error if the process failed due to any reason, nil otherwise.
    public function cancel() returns error? {
        return self.stop();
    }

    # Pauses the task.
    #
    # + return - Returns error if the task is not running or any other error is occured, nil Otherwise.
    public extern function pause() returns error?;

    # Resumes a paused task.
    #
    # + return - Returns error is the task is not paused or, any other error occurred, nil Otherwise.
    public extern function resume() returns error?;

    # Detach the service from the listener.
    #
    # + attachedService - service which needs to be detached from the listener.
    # + return - Returns error if the process failed due to any reason, nil otherwise.
    extern function detach(service attachedService) returns error?;
};
