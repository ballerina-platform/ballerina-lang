// Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

# Configurations related to a timer, which are used to define the behavior of a timer when initializing the
# `task:Listener`.
#
# + intervalInMillis - Timer interval (in milliseconds), which triggers the `onTrigger` resource
# + initialDelayInMillis - Delay (in milliseconds) after which the timer will run
# + noOfRecurrences - Number of times to trigger the task after which the task stops running
public type TimerConfiguration record {|
    int intervalInMillis;
    int initialDelayInMillis?;
    int noOfRecurrences?;
|};

# Configurations related to an appointment, which are used to define the behavior of an appointment when initializing
# the `task:Listener`.
#
# + appointmentDetails - A CRON expression as a string or `task:AppointmentData` for scheduling an appointment
# + noOfRecurrences - Number of times to trigger the task after which the task stops running
public type AppointmentConfiguration record {|
    string|AppointmentData appointmentDetails;
    int noOfRecurrences?;
|};

# The CRON expression required for scheduling an appointment.
#
# + seconds - Second(s) in a given minute in which the appointment will run
# + minutes - Minute(s) in a given hour in which the appointment will run
# + hours - Hour(s) in a given day in which the appointment will run
# + daysOfMonth - Day(s) of the month in which the appointment will run
# + months - Month(s) in a given year in which the appointment will run
# + daysOfWeek - Day(s) of a week in which the appointment will run
# + year - Year(s) in which the appointment will run
public type AppointmentData record {|
    string seconds?;
    string minutes?;
    string hours?;
    string daysOfMonth?;
    string months?;
    string daysOfWeek?;
    string year?;
|};
