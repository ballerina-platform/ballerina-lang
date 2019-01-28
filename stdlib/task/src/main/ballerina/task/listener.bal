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
public type TimerConfiguration record {
    int interval;
    int delay?;
    !...;
};

# Configurations related to an Appointment
#
# + cronExpression - Cron expression for the appointment. Providing cronExpression will override all the other configurations.
# + seconds - Second(s) in a given minute, in which the appointment will run.
# + minutes - Minute(s) in a given hour, in which the appointment will run.
# + hours - Hour(s) in a given day, in which the appointment will run.
# + dayOfMonth - Day(s) of the month, in which the appointment will run.
# + month - Month(s) in a given year, in which the appointment will run.
# + dayOfWeek - Day(s) of a week, in which the appointment will run.
# + year - Year(s) in which the appointment will run.
public type AppointmentConfiguration record {
    string cronExpression?;
    string seconds?;
    string minutes?;
    string hours?;
    string daysOfMonth?;
    string months?;
    string daysOfWeek?;
    string year?;
    !...;
};

# Represents a ballerina task listener
#
# + listenerConfiguration - Provide configurations related to task.
public type Listener object {
    *AbstractListener;

    private TimerConfiguration|AppointmentConfiguration listenerConfiguration;
    private string taskId;

    public function __init(TimerConfiguration|AppointmentConfiguration configs) {
        self.listenerConfiguration = configs;
    }

    public function __attach(service s, map<any> annotationData) returns error? {
        return self.register(s, annotationData);
    }

    public function __start() returns error? {
        return self.start();
    }

    public function __stop() returns error? {

    }

    extern function register(service attachedService, map<any> annotationData) returns error?;

    extern function start() returns error?;
};
