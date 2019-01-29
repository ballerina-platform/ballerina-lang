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

public const TIME_FORMAT_RFC_1123 = "RFC_1123";

public type TimeFormat "RFC_1123";

# Ballerina TimeZone represents the time-zone information associated with a particular time.
#
# + is - Zone short ID or offset string
# + offset - The offset in seconds
public type TimeZone record {
    string id;
    int offset = 0;
    !...;
};

# Ballerina Time represents a particular time with its associated time-zone.
#
# + time - Time value as milliseconds since epoch
# + zone - The time zone of the time
public type Time record {
    int time;
    TimeZone zone;
    !...;
};
# Returns ISO 8601 string representation of the given time.
#
# + time - The Time record to be converted to string
# + return - The ISO 8601 formatted string of the given time
public extern function toString(Time time) returns string;

# Returns formatted string representation of the given time.
#
# + time - The Time record to be formatted
# + timeFormat - The format which is used to format the time represented by this object
# + return - The formatted string of the given time
public extern function format(Time time, string|TimeFormat timeFormat) returns string;

# Returns the year representation of the given time.
#
# + time - The Time record to retrieve the year representation from
# + return - The year representation
public extern function getYear(Time time) returns int;

# Returns the month representation of the given time.
#
# + time - The Time record to get the month representation from
# + return - The month-of-year, from 1 (January) to 12 (December)
public extern function getMonth(Time time) returns int;

# Returns the date representation of the given time.
#
# + time - The Time record to get the date representation from
# + return - The day-of-month, from 1 to 31
public extern function getDay(Time time) returns int;

# Returns the weekday representation of the given time.
#
# + time - The Time record to get the weekday representation from
# + return - The weekday representation from SUNDAY to SATURDAY
public extern function getWeekday(Time time) returns string;

# Returns the hour representation of the given time.
#
# + time - The Time record to get the  hour representation from
# + return - The hour-of-day, from 0 to 23
public extern function getHour(Time time) returns int;

# Returns the minute representation of the given time.
#
# + time - The Time record to get the  minute representation from
# + return - The minute-of-hour to represent, from 0 to 59
public extern function getMinute(Time time) returns int;

# Returns the second representation of the given time.
#
# + time - The Time record to get the second representation from
# + return - The second-of-minute, from 0 to 59
public extern function getSecond(Time time) returns int;

# Returns the millisecond representation of the given time.
#
# + time - The Time record to get the millisecond representation from
# + return - The milli-of-second, from 0 to 999
public extern function getMilliSecond(Time time) returns int;

# Returns the date representation of the given time.
#
# + time - The Time record to get the date representation from
# + return - The year representation.
#            The month-of-year, from 1 (January) to 12 (December).
#            The day-of-month, from 1 to 31.
public extern function getDate(Time time) returns (int, int, int);

# Returns the time representation of the given time.
#
# + time - The Time record
# + return - The hour-of-day, from 0 to 23.
#            The minute-of-hour to represent, from 0 to 59.
#            The second-of-minute, from 0 to 59.
#            The milli-of-second, from 0 to 999.
public extern function getTime(Time time) returns (int, int, int, int);

# Add specified durations to the given time value.
#
# + time - The Time record to add the duration to
# + years - The year representation
# + months - The month-of-year to represent, from 1 (January) to 12 (December)
# + days - The day-of-month to represent, from 1 to 31
# + hours - The hour-of-day to represent, from 0 to 23
# + minutes - The minute-of-hour to represent, from 0 to 59
# + seconds - The second-of-minute to represent, from 0 to 59
# + milliSeconds - The milli-of-second to represent, from 0 to 999
# + return - Time object containing time and zone information after the addition
public extern function addDuration(Time time, int years, int months, int days, int hours, int minutes, int seconds,
                                   int milliSeconds) returns Time;

# Subtract specified durations from the given time value.
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
public extern function subtractDuration(Time time, int years, int months, int days, int hours, int minutes, int seconds,
                                        int milliSeconds) returns Time;

# Change the time-zone of the given time.
#
# + time - The Time record of which the time-zone to be changed
# + zoneId - The new time-zone id
# + return - Time object containing time and zone information after the conversion
public extern function toTimeZone(Time time, string zoneId) returns Time;

# Returns the current time value with the system default time-zone.
#
# + return - Time object containing the time and zone information
public extern function currentTime() returns Time;

# Returns the current system time in nano seconds.
#
# + return - Int value of the current system time in nano seconds
public extern function nanoTime() returns int;

# Returns the Time object correspoding to the given time components and time-zone.
#
# + year - The year representation
# + month - The month-of-year to represent, from 1 (January) to 12 (December)
# + date - The day-of-month to represent, from 1 to 31
# + hour - The hour-of-day to represent, from 0 to 23
# + minute - The minute-of-hour to represent, from 0 to 59
# + second - The second-of-minute to represent, from 0 to 59
# + milliSecond - The milli-of-second to represent, from 0 to 999
# + zoneId - The zone id of the required time-zone.If empty the system local time-zone will be used
# + return - Time object containing time and zone information
public extern function createTime(int year, int month, int date, int hour, int minute, int second, int milliSecond,
                                  string zoneId) returns Time;

# Returns the time for the given string representation based on the given format string.
#
# + data - The time text to parse
# + timeFormat - The format which is used to parse the given text
# + return - Time object containing time and zone information
public extern function parse(string data, string|TimeFormat timeFormat) returns Time;
