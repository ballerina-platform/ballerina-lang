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

package ballerina.time;

@final public TimeFormat TIME_FORMAT_RFC_1123 = "RFC_1123";

public type TimeFormat "RFC_1123";

@Description {value:"Ballerina Timezone represents the timezone information associated with a particular time."}
@Field {value:"zoneId: Zone short ID or offset string."}
@Field {value:"zoneOffset: The offset in seconds."}
public type Timezone {
    string zoneId,
    int zoneOffset,
};

@Description {value:"Ballerina Time represents a particular time with its associated timezone."}
@Field {value:"time: Time value as milliseconds since epoch."}
@Field {value:"zone: The time   zone of the time."}
public type Time object {
    public {
        int time;
        Timezone zone;
    }

    public new(time, zone) {}

    @Description {value:"Returns ISO 8601 string representation of the given time."}
    @Param {value:"time: The time object for which needs to get the string representation"}
    @Return {value:"The ISO 8601 formatted string of the given time."}
    public native function toString() returns (string);

    @Description {value:"Returns formatted string representation of the given time."}
    @Param {value:"time: The time object for which needs to get the string representation"}
    @Param {value:"format: The format which is used to format the given text"}
    @Return {value:"The formatted string of the given time."}
    public native function format(string format) returns (string);

    @Description {value:"Formats the given string to the specified standard time format and returns the formatted string."}
    @Param {value:"time: The time object for which the string representation is needed"}
    @Param {value:"format: The format which is used to format the given text"}
    @Return {value:"The formatted string of the given time."}
    public native function formatTo(TimeFormat format) returns (string);

    @Description {value:"Returns the year representation of the given time."}
    @Param {value:"time: The time object which needs to get the year representation"}
    @Return {value:"The year representation."}
    public native function year() returns (int);

    @Description {value:"Returns the month representation of the given time."}
    @Param {value:"time: The time object which needs to get the month representation"}
    @Return {value:"The month-of-year, from 1 (January) to 12 (December)."}
    public native function month() returns (int);

    @Description {value:"Returns the date representation of the given time."}
    @Param {value:"time: The time object which needs to get the date representation"}
    @Return {value:"The day-of-month, from 1 to 31."}
    public native function day() returns (int);

    @Description {value:"Returns the weekday representation of the given time."}
    @Param {value:"time: The time object which needs to get the weekday representation"}
    @Return {value:"The weekday representation from SUNDAY to SATURDAY."}
    public native function weekday() returns (string);

    @Description {value:"Returns the hour representation of the given time."}
    @Param {value:"time: The time object which needs to get the hour representation"}
    @Return {value:"The hour-of-day, from 0 to 23."}
    public native function hour() returns (int);

    @Description {value:"Returns the minute representation of the given time."}
    @Param {value:"time: The time object which needs to get the minute representation"}
    @Return {value:"The minute-of-hour to represent, from 0 to 59."}
    public native function minute() returns (int);

    @Description {value:"Returns the second representation of the given time."}
    @Param {value:"time: The time object which needs to get the second representation"}
    @Return {value:"The second-of-minute, from 0 to 59."}
    public native function second() returns (int);

    @Description {value:"Returns the millisecond representation of the given time."}
    @Param {value:"time: The time object which needs to get the millisecond representation"}
    @Return {value:"The milli-of-second, from 0 to 999."}
    public native function milliSecond() returns (int);

    @Description {value:"Returns the date representation of the given time."}
    @Param {value:"time: The time object which needs to get the date representation"}
    @Return {value:"The year representation."}
    @Return {value:"The month-of-year, from 1 (January) to 12 (December)."}
    @Return {value:"The day-of-month, from 1 to 31."}
    public native function getDate() returns (int, int, int);

    @Description {value:"Returns the time representation of the given time."}
    @Param {value:"time: The time object which needs to get the time representation"}
    @Return {value:"The hour-of-day, from 0 to 23."}
    @Return {value:"The minute-of-hour to represent, from 0 to 59."}
    @Return {value:"The second-of-minute, from 0 to 59."}
    @Return {value:"The milli-of-second, from 0 to 999."}
    public native function getTime() returns (int, int, int, int);

    @Description {value:"Add specified durations to the given time value."}
    @Param {value:"time: The time object which needs to add the given duration"}
    @Param {value:"years: The year representation"}
    @Param {value:"months: The month-of-year to represent, from 1 (January) to 12 (December)"}
    @Param {value:"days: The day-of-month to represent, from 1 to 31"}
    @Param {value:"hours: The hour-of-day to represent, from 0 to 23"}
    @Param {value:"minutes: The minute-of-hour to represent, from 0 to 59"}
    @Param {value:"seconds: The second-of-minute to represent, from 0 to 59"}
    @Param {value:"milliSeconds: The milli-of-second to represent, from 0 to 999"}
    @Return {value:"Time object containing time and zone information after the addition."}
    public native function addDuration(int years, int months, int days, int hours, int minutes, int seconds,
    int milliSeconds) returns (Time);

    @Description {value:"Subtract specified durations from the given time value."}
    @Param {value:"years: The year representation"}
    @Param {value:"months: The month-of-year to represent, from 1 (January) to 12 (December)"}
    @Param {value:"days: The day-of-month to represent, from 1 to 31"}
    @Param {value:"hours: The hour-of-day to represent, from 0 to 23"}
    @Param {value:"minutes: The minute-of-hour to represent, from 0 to 59"}
    @Param {value:"seconds: The second-of-minute to represent, from 0 to 59"}
    @Param {value:"milliSeconds: The milli-of-second to represent, from 0 to 999"}
    @Return {value:"Time object containing time and zone information after the subtraction."}
    public native function subtractDuration(int years, int months, int days, int hours, int minutes, int seconds,
    int milliSeconds) returns (Time);

    @Description {value:"Change the timezone of the given time."}
    @Param {value:"time: The time object which needs to change the timezone information"}
    @Param {value:"zoneId: The new timezone id"}
    @Return {value:"Time object containing time and zone information after the conversion."}
    public native function toTimezone(string zoneId) returns (Time);
};

@Description {value:"Returns the current time value with the system default timezone."}
@Return {value:"Time object containing the time and zone information."}
public native function currentTime() returns (Time);

@Description {value:"Returns the current system time in nano seconds"}
@Return {value:"Int value of the current system time in nano seconds"}
public native function nanoTime() returns (int);

@Description {value:"Returns the Time object correspoding to the given time components and timezone."}
@Param {value:"year: The year representation"}
@Param {value:"month: The month-of-year to represent, from 1 (January) to 12 (December)"}
@Param {value:"date: The day-of-month to represent, from 1 to 31"}
@Param {value:"hour: The hour-of-day to represent, from 0 to 23"}
@Param {value:"minute: The minute-of-hour to represent, from 0 to 59"}
@Param {value:"second: The second-of-minute to represent, from 0 to 59"}
@Param {value:"milliSecond: The milli-of-second to represent, from 0 to 999"}
@Param {value:"zoneId: The zone id of the required timezone.If empty the system local timezone will be used"}
@Return {value:"Time object containing time and zone information."}
public native function createTime(int year, int month, int date, int hour, int minute, int second, int milliSecond,
string zoneId) returns (Time);

@Description {value:"Returns the time for the given string representation based on the given format string."}
@Param {value:"data: The time text to parse"}
@Param {value:"format: The format which is used to parse the given text"}
@Return {value:"Time object containing time and zone information."}
public native function parse(string data, string format) returns (Time);

@Description {value:"Returns the time for the given string representation based on the specified standard time format."}
@Param {value:"timestamp: The time text to parse"}
@Param {value:"format: The format which is used to parse the given date/time string"}
@Return {value:"Time object containing time and zone information."}
public native function parseTo(string timestamp, TimeFormat format) returns Time;
