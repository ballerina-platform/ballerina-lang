## Module overview

This module provides implementations related to time, date, time zones, and durations. 

The module has two main types as [Time](records/Time.html) and [TimeZone](records/TimeZone.html). The type `Time` represents a time associated with a given time zone. It has `time` and `zone` as attributes. The type `TimeZone` represents the time zone associated with a given time. It has `id` and `offset` as attributes. An `id` can be one of the following:

* If `id` equals 'Z', the result is UTC.
* If `id` equals 'GMT', 'UTC' or 'UT', it is equivalent to UTC.
* If `id` starts with '+' or '-', the ID is parsed as an offset. Offset can be specified in one of the following ways. +h, +hh, +hh:mm, -hh:mm, +hhmm, -hhmm, +hh:mm:ss, -hh:mm:ss, +hhmmss, -hhmmss
* Also `id` can be a region-based zone ID. The format is '{area}/{city}' eg: "America/Panama". The zones are based on IANA Time Zone Database (TZDB) supplied data.

## Samples

### Getting the current time/date

```ballerina
import ballerina/io;
import ballerina/time;

public function main() {
    time:Time time = time:currentTime();    // Create a record of type ‘Time’.
    io:println(time);

    int timeValue = time.time;    // Time in milliseconds since January 1, 1970, 00:00:00 GMT. E.g., 1523513039.
    io:println(timeValue);
    int nanoTime = time:nanoTime();    // Time in nanoseconds since an arbitrary origin. Therefore, it should be used only to calculate durations. E.g., 2426115697486340.
    io:println(nanoTime);
    string zoneId = time.zone.id;    // Time zone as an identifier. E.g., “America/Panama”.
    io:println(zoneId);
    int zoneoffset = time.zone.offset;    // Time zone as an offset. E.g., -05:00.
    io:println(zoneoffset);

    // Get the current date and time upto milliseconds.
    int year = time:getYear(time);    // E.g., 2018
    io:println(year);
    int month = time:getMonth(time);    // E.g., 3
    io:println(month);
    int day = time:getDay(time);    // E.g., 1
    io:println(day);
    int hour = time:getHour(time);    // E.g., 18
    io:println(hour);
    int minute = time:getMinute(time);    // E.g., 56
    io:println(minute);
    int second = time:getSecond(time);    // E.g., 23
    io:println(second);
    int milliSecond = time:getMilliSecond(time);    // E.g., 555
    io:println(milliSecond);
    string weekday = time:getWeekday(time);// Day of the week. E.g., “TUESDAY”.
    io:println(weekday);
}
```

### Creating a time/date record

```ballerina
import ballerina/io;
import ballerina/time;

public function main() {
    // Create a record of type ‘Time’ with time zone.
    time:TimeZone zoneIdValue = {id: "America/Panama"};
    time:Time time1 = {time: 1498488382000, zone: zoneIdValue};
    io:println(time1);

    // Create a record of type ‘Time’ with the time zone offset.
    time:TimeZone zoneOffsetValue = {id: "-05:00"};
    time:Time time2 = {time: 1498488382000, zone: zoneOffsetValue};
    io:println(time2);

    // Create a record of type ‘Time’ without the time zone.
    time:TimeZone noZoneValue = {id: ""};
    time:Time time3 = {time: 1498488382000, zone: noZoneValue};
    io:println(time3);

    // Create a record of type ‘Time’ with time and date. E.g., 2018-03-28T23:42:45.554-05:00
    time:Time | error dateTime = time:createTime(2018, 3, 28, 23, 42, 45, 554, "America/Panama");
    if (dateTime is time:Time) {
        io:println(dateTime);
    } else {
        io:println("An error occurred: ", dateTime);
    }
}
```


### Formatting a time/date to a string

```ballerina
import ballerina/io;
import ballerina/time;

public function main() returns error? {
    time:TimeZone zoneValue = {id: "America/Panama"};
    time:Time time = {time: 1498488382444, zone: zoneValue};

    //Format a time to a string of a given pattern.
    string | error time1 = check time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");    //E.g., “2017-06-26T09:46:22.444-0500”.
    io:println(time1);

    //Format a time to a string of the RFC-1123 format.
    string | error time2 = time:format(time, time:TIME_FORMAT_RFC_1123);    // E.g., "Mon, 26 Jun 2017 09:46:22 -0500”
    io:println(time2);

    // Convert a time record to a string value.
    string time3 = time:toString(time);//”2017-06-26T09:46:22.444-05:00”
    io:println(time3);
}
```

### Parsing a string to time/date

```ballerina
import ballerina/io;
import ballerina/time;

public function main() returns error? {
    // Parse a time string of a given format.
    time:Time | error time1 = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");    // The ‘Z’ stands for the time zone.
    io:println(time1);

    // Parse a time string of the RFC-1123 format.
    time:Time | error time2 = time:parse("Wed, 28 Mar 2018 11:56:23 +0530", time:TIME_FORMAT_RFC_1123);
    io:println(time2);
}
```

### Setting time durations

```ballerina
import ballerina/time;

public function main() {
    // Add a duration to a given time.
    time:Time | error time1 = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (time1 is time:Time) {
        time1 = time:addDuration(time1, 1, 1, 1, 1, 1, 1, 1);
    // Adds 1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second, and 1 millisecond.
    }

    // Subtract a duration from a given time.
    time:Time | error time2 = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (time2 is time:Time) {
        time2 = time:subtractDuration(time2, 1, 1, 1, 1, 1, 1, 1);
    // Subtracts 1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second, and 1 millisecond.
    }

    // Get the duration between two times.
    time:Time time3 = time:currentTime();
    time:Time time4 = time:currentTime();
    int diffInMillis = time3.time - time4.time;
}
```
## Patterns for formatting and parsing

The below patterns can be used to generate the formatter string when using the `format()` and `parse()` functions.

**Symbol**|**Meaning**|**Presentation**|**Examples**
:-----:|:-----:|:-----:|:-----:
G|era|text|AD; Anno Domini; A
u|year|year|2004; 04
y|year-of-era|year|2004; 04
D|day-of-year|number|189
M/L|month-of-year|number/text|7; 07; Jul; July; J
d|day-of-month|number|10
Q/q|quarter-of-year|number/text|3; 03; Q3; 3rd quarter
Y|week-based-year|year|1996; 96
w|week-of-week-based-year|number|27
W|week-of-month|number|4
E|day-of-week|text|Tue; Tuesday; T
e/c|localized day-of-week|number/text|2; 02; Tue; Tuesday; T
F|week-of-month|number|3  
a|cam-pm-of-day|text|PM
h|clock-hour-of-am-pm (1-12)|number|12
K|hour-of-am-pm (0-11)|number|0
k|clock-hour-of-am-pm (1-24)|number|0
H|hour-of-day (0-23)|number|0
m|minute-of-hour|number|30
s|second-of-minute|number|55
S|fraction-of-second|fraction|978
A|milli-of-day|number|1234
n|nano-of-second|number|987654321
N|nano-of-day|number|1234000000
V|ime-zone ID|zone-id|America/Los\_Angeles; Z; -08:30
z|time-zone name|zone-name|Pacific Standard Time; PST
O|localized zone-offset|offset-O|GMT+8; GMT+08:00; UTC-08:00
X|zone-offset 'Z' for zero|offset-X|Z; -08; -0830; -08:30; -083015; -08:30:15
x|zone-offset|offset-x|+0000; -08; -0830; -08:30; -083015; -08:30:15
Z|zone-offset|offset-Z|+0000; -0800; -08:00
p|pad next|pad modifier|1
'|escape for text|delimiter|
''|single quote|literal|'
[|optional section start|
]|optional section end
