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

@final public TimeFormat TIME_FORMAT_RFC_1123 = "RFC_1123";

public type TimeFormat "RFC_1123";

documentation {
    Ballerina Timezone represents the timezone information associated with a particular time.
    F{{zoneId}} Zone short ID or offset string
    F{{zoneOffset}} The offset in seconds
}
public type Timezone record {
    string zoneId,
    int zoneOffset,
};

documentation {
    Ballerina Time represents a particular time with its associated timezone.
    F{{time}} Time value as milliseconds since epoch
    F{{zone}} The time zone of the time
}
public type Time object {

    public int time;
    public Timezone zone;

    public new (time, zone) {}

    documentation {
        Returns ISO 8601 string representation of the given time.

        R{{}} The ISO 8601 formatted string of the given time
    }
    public extern function toString() returns (string);

    documentation {
        Returns formatted string representation of the given time.

        P{{format}} The format which is used to format the time represented by this object
        R{{}} The formatted string of the given time
    }
    public extern function format(string|TimeFormat format) returns (string);

    documentation {
        Returns the year representation of the given time.

        R{{}} The year representation
    }
    public extern function year() returns (int);

    documentation {
        Returns the month representation of the given time.

        R{{}} The month-of-year, from 1 (January) to 12 (December)
    }
    public extern function month() returns (int);

    documentation {
        Returns the date representation of the given time.

        R{{}} The day-of-month, from 1 to 31
    }
    public extern function day() returns (int);

    documentation {
        Returns the weekday representation of the given time.

        R{{}} The weekday representation from SUNDAY to SATURDAY
    }
    public extern function weekday() returns (string);

    documentation {
        Returns the hour representation of the given time.

        R{{}} The hour-of-day, from 0 to 23
    }
    public extern function hour() returns (int);

    documentation {
        Returns the minute representation of the given time.

        R{{}} The minute-of-hour to represent, from 0 to 59
    }
    public extern function minute() returns (int);

    documentation {
        Returns the second representation of the given time.

        R{{}} The second-of-minute, from 0 to 59
    }
    public extern function second() returns (int);

    documentation {
        Returns the millisecond representation of the given time.

        R{{}} The milli-of-second, from 0 to 999
    }
    public extern function milliSecond() returns (int);

    documentation {
        Returns the date representation of the given time.

        R{{}} The year representation
        R{{}} The month-of-year, from 1 (January) to 12 (December)
        R{{}} The day-of-month, from 1 to 31
    }
    public extern function getDate() returns (int, int, int);

    documentation {
        Returns the time representation of the given time.

        R{{}} The hour-of-day, from 0 to 23
        R{{}} The minute-of-hour to represent, from 0 to 59
        R{{}} The second-of-minute, from 0 to 59
        R{{}} The milli-of-second, from 0 to 999
    }
    public extern function getTime() returns (int, int, int, int);

    documentation {
        Add specified durations to the given time value.

        P{{years}} The year representation
        P{{months}} The month-of-year to represent, from 1 (January) to 12 (December)
        P{{days}} The day-of-month to represent, from 1 to 31
        P{{hours}} The hour-of-day to represent, from 0 to 23
        P{{minutes}} The minute-of-hour to represent, from 0 to 59
        P{{seconds}} The second-of-minute to represent, from 0 to 59
        P{{milliSeconds}} The milli-of-second to represent, from 0 to 999
        R{{}} Time object containing time and zone information after the addition
    }

    public extern function addDuration(int years, int months, int days, int hours, int minutes, int seconds,
                                       int milliSeconds) returns (Time);

    documentation {
        Subtract specified durations from the given time value.

        P{{years}} The year representation
        P{{months}} The month-of-year to represent, from 1 (January) to 12 (December)
        P{{days}} The day-of-month to represent, from 1 to 31
        P{{hours}} The hour-of-day to represent, from 0 to 23
        P{{minutes}} The minute-of-hour to represent, from 0 to 59
        P{{seconds}} The second-of-minute to represent, from 0 to 59
        P{{milliSeconds}} The milli-of-second to represent, from 0 to 999
        R{{}} Time object containing time and zone information after the subtraction
    }
    public extern function subtractDuration(int years, int months, int days, int hours, int minutes, int seconds,
                                            int milliSeconds) returns (Time);

    documentation {
        Change the timezone of the given time.

        P{{zoneId}} The new timezone id
        R{{}} Time object containing time and zone information after the conversion
    }
    public extern function toTimezone(string zoneId) returns (Time);
};

documentation {
    Returns the current time value with the system default timezone.

    R{{}} Time object containing the time and zone information
}
public extern function currentTime() returns (Time);

documentation {
    Returns the current system time in nano seconds.

    R{{}} Int value of the current system time in nano seconds
}
public extern function nanoTime() returns (int);

documentation {
    Returns the Time object correspoding to the given time components and timezone.

    P{{year}} The year representation
    P{{month}} The month-of-year to represent, from 1 (January) to 12 (December)
    P{{date}} The day-of-month to represent, from 1 to 31
    P{{hour}} The hour-of-day to represent, from 0 to 23
    P{{minute}} The minute-of-hour to represent, from 0 to 59
    P{{second}} The second-of-minute to represent, from 0 to 59
    P{{milliSecond}} The milli-of-second to represent, from 0 to 999
    P{{zoneId}} The zone id of the required timezone.If empty the system local timezone will be used
    R{{}} Time object containing time and zone information
}
public extern function createTime(int year, int month, int date, int hour, int minute, int second, int milliSecond,
                                  string zoneId) returns (Time);

documentation {
    Returns the time for the given string representation based on the given format string.

    P{{data}} The time text to parse
    P{{format}} The format which is used to parse the given text
    R{{}} Time object containing time and zone information
}
public extern function parse(string data, string|TimeFormat format) returns (Time);
