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

import ballerina/java;

# Specifies the time format defined by the RFC-1123.
public const TIME_FORMAT_RFC_1123 = "RFC_1123";

# The time format defined by the RFC-1123.
public type TimeFormat TIME_FORMAT_RFC_1123;

# Represents the time-zone information associated with a particular time.
#
# + id - Zone short ID or offset string
# + offset - The offset in seconds
public type TimeZone record {|
    string id;
    int offset = 0;
|};

# Represents a particular time with its associated time-zone.
#
# + time - Time value as milliseconds since epoch
# + zone - The time zone of the time
public type Time record {|
    int time;
    TimeZone zone;
|};

# Returns the ISO 8601 string representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  string timeString = time:toString(time);
# ```
#
# + time - The Time record to be converted to string
# + return - The ISO 8601-formatted string of the given time
public function toString(Time time) returns string = @java:Method {
    name: "toString",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods",
    paramTypes: ["io.ballerina.runtime.values.MapValue"]
} external;

# Returns the formatted string representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  string|error timeString = time:format(time, time:TIME_FORMAT_RFC_1123);
# ```
#
# + time - The Time record to be formatted
# + timeFormat - The format, which is used to format the time represented by this object
# + return - The formatted string of the given time or else a `time:Error` if failed to format the time
public function format(Time time, string timeFormat) returns string|Error = @java:Method {
    name: "format",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the year representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  int year = time:getYear(time);
# ```
#
# + time - The Time record to retrieve the year representation
# + return - The year representation
public function getYear(Time time) returns int = @java:Method {
    name: "getYear",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the month representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  int month = time:getMonth(time);
# ```
#
# + time - The Time record to get the month representation from
# + return - The month-of-year from 1 (January) to 12 (December)
public function getMonth(Time time) returns int = @java:Method {
    name: "getMonth",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the date representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  int day = time:getDay(time);
# ```
#
# + time - The Time record to get the date representation
# + return - The day-of-month from 1 to 31
public function getDay(Time time) returns int = @java:Method {
    name: "getDay",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the weekday representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  string weekDay = time:getWeekday(time);
# ```
#
# + time - The Time record to get the weekday representation
# + return - The weekday representation from SUNDAY to SATURDAY
public function getWeekday(Time time) returns string = @java:Method {
    name: "getWeekday",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the hour representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  int hour = time:getHour(time);
# ```
#
# + time - The Time record to get the hour representation
# + return - The hour-of-day from 0 to 23
public function getHour(Time time) returns int = @java:Method {
    name: "getHour",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the minute representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  int minute = time:getMinute(time);
# ```
#
# + time - The Time record to get the minute representation
# + return - The minute-of-hour to represent from 0 to 59
public function getMinute(Time time) returns int = @java:Method {
    name: "getMinute",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the second representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  int second = time:getSecond(time);
# ```
#
# + time - The Time record to get the second representation
# + return - The second-of-minute from 0 to 59
public function getSecond(Time time) returns int = @java:Method {
    name: "getSecond",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the millisecond representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  int milliSecond = time:getMilliSecond(time);
# ```
#
# + time - The Time record to get the millisecond representation
# + return - The milli-of-second from 0 to 999
public function getMilliSecond(Time time) returns int = @java:Method {
    name: "getMilliSecond",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the date representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  [int, int, int] date = time:getDate(time);
# ```
#
# + time - The Time record to get the date representation
# + return - The year representation with
#            the month-of-year from 1 (January) to 12 (December) and 
#            the day-of-month from 1 to 31
public function getDate(Time time) returns [int, int, int] = @java:Method {
    name: "getDate",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the time representation of the given time.
# ```ballerina
#  time:TimeZone zoneValue = {id: "America/Panama"};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  [int, int, int, int] timeGenerated = time:getTime(time);
# ```
#
# + time - The Time record
# + return - The hour-of-day to represent from 0 to 23,
#            the minute-of-hour to represent from 0 to 59,
#            the second-of-minute from 0 to 59,
#            and the milli-of-second from 0 to 999
public function getTime(Time time) returns [int, int, int, int] = @java:Method {
    name: "getTime",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Add specified durations to the given time value.
# ```ballerina
#  string timeText = "2020-06-26T09:46:22.444-0500";
#  string timeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
#  time:Time|error originalTime = time:parse(timeText, timeFormat);
#  if (originalTime is time:Time) {
#      time:Time newTime = time:addDuration(originalTime, 1, 1, 1, 1, 1, 1, 1);
#  }
# ```
#
# + time - The Time record to add the duration 
# + years - The year representation
# + months - The month-of-year to represent, from 1 (January) to 12 (December)
# + days - The day-of-month to represent, from 1 to 31
# + hours - The hour-of-day to represent, from 0 to 23
# + minutes - The minute-of-hour to represent, from 0 to 59
# + seconds - The second-of-minute to represent, from 0 to 59
# + milliSeconds - The milli-of-second to represent, from 0 to 999
# + return - Time object containing time and zone information after the addition
public function addDuration(Time time, int years, int months, int days, int hours, int minutes, int seconds,
                            int milliSeconds) returns Time = @java:Method {
    name: "addDuration",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Subtract specified durations from the given time value.
# ```ballerina
#  string timeText = "2020-06-26T09:46:22.444-0500";
#  string timeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
#  time:Time|error originalTime = time:parse(timeText, timeFormat);
#  if (originalTime is time:Time) {
#      time:Time newTime = time:subtractDuration(originalTime, 1, 1, 1, 1, 1, 1, 1);
#  }
# ```
#
# + time - The Time record to subtract the duration from
# + years - The year representation
# + months - The month-of-year to represent, from 1 (January) to 12 (December)
# + days - The day-of-month to represent, from 1 to 31
# + hours - The hour-of-day to represent, from 0 to 23
# + minutes - The minute-of-hour to represent, from 0 to 59
# + seconds - The second-of-minute to represent, from 0 to 59
# + milliSeconds - The milli-of-second to represent, from 0 to 999
# + return - Time object containing time and zone information after the subtraction
public function subtractDuration(Time time, int years, int months, int days, int hours, int minutes, int seconds,
                                 int milliSeconds) returns Time = @java:Method {
    name: "subtractDuration",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Change the time-zone of the given time.
# ```ballerina
#  string zoneId = "America/Panama";
#  time:TimeZone zoneValue = {id: zoneId};
#  time:Time time = {time: 1578488382444, zone: zoneValue};
#  time:Time|time:Error newTime = time:toTimeZone(time, zoneId);
# ```
#
# + time - The Time record of which the time-zone is to be changed
# + zoneId - The new time-zone ID
# + return - Time object containing the time and zone information after the conversion
#            or else a `time:Error` if failed to format the time
public function toTimeZone(Time time, string zoneId) returns Time|Error = @java:Method {
    name: "toTimeZone",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the current time value with the default system time-zone.
# ```ballerina
#  time:Time now = time:currentTime();
# ```
#
# + return - Time object containing the time and the zone information
public function currentTime() returns Time = @java:Method {
    name: "currentTime",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the current system time in nano seconds.
# ```ballerina
#  int now = time:nanoTime();
# ```
#
# + return - Integer value of the current system time in nano seconds
public function nanoTime() returns int = @java:Method {
    name: "nanoTime",
    'class: "java.lang.System"
} external;

# Returns the Time object correspoding to the given time components and time-zone.
# ```ballerina
#  time:Time|time:Error dateTime = time:createTime(2020, 3, 28, 23, 42, 45, 554, "America/Panama");
# ```
#
# + year - The year representation
# + month - The month-of-year to represent from 1 (January) to 12 (December)
# + date - The day-of-month to represent from 1 to 31
# + hour - The hour-of-day to represent from 0 to 23
# + minute - The minute-of-hour to represent from 0 to 59
# + second - The second-of-minute to represent, from 0 to 59
# + milliSecond - The milli-of-second to represent, from 0 to 999
# + zoneId - The zone id of the required time-zone.If empty the system local time-zone will be used
# + return - Time object containing time and zone information or an `time:Error` if failed to create the time
public function createTime(int year, int month, int date, int hour, int minute, int second, int milliSecond,
                           string zoneId) returns Time|Error = @java:Method {
    name: "createTime",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;

# Returns the time for the given string representation based on the given format string.
# ```ballerina
#  string timeFormat = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
#  time:Time|time:Error time = time:parse("2020-06-26T09:46:22.444-0500", timeFormat);
# ```
#
# + data - The time text to parse
# + timeFormat - The format, which is used to parse the given text
# + return - Time object containing the time and zone information or else  a `time:Error` if failed to parse the given string
public function parse(string data, string timeFormat) returns Time|Error = @java:Method {
    name: "parse",
    'class: "org.ballerinalang.stdlib.time.nativeimpl.ExternMethods"
} external;
