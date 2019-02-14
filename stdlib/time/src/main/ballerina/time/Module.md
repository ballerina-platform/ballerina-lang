## Module overview
The `ballerina/time` module provides implementations related to time, date, time zones, and durations. 

The module has two main types as [Time](time.html#Time) and [TimeZone](time.html#TimeZone). The type `Time` represents a time associated with a given time zone. It has `time` and `zone` as attributes. The type `TimeZone` represents the time zone associated with a given time. It has `id` and `offset` as attributes. An `id` can be one of the following:

* If `id` equals 'Z', the result is UTC.
* If `id` equals 'GMT', 'UTC' or 'UT', it is equivalent to UTC.
* If `id` starts with '+' or '-', the ID is parsed as an offset. Offset can be specified in one of the following ways. +h, +hh, +hh:mm, -hh:mm, +hhmm, -hhmm, +hh:mm:ss, -hh:mm:ss, +hhmmss, -hhmmss
* Also `id` can be a region-based zone ID. The format is '{area}/{city}' eg: "America/Panama". The zones are based on IANA Time Zone Database (TZDB) supplied data.

## Samples

### Getting the current time/date

```ballerina
time:Time time = time:currentTime(); // Create a record of type ‘Time’.

int timeValue = time.time;  // Time in milliseconds since January 1, 1970, 00:00:00 GMT. E.g., 1523513039.
int nanoTime = time:nanoTime(); // Time in nanoseconds since an arbitrary origin. Therefore, it should be used only to calculate durations. E.g., 2426115697486340.
string zoneId = time.zone.id; // Time zone as an identifier. E.g., “America/Panama”.
int zoneoffset = time.zone.offset; // Time zone as an offset. E.g., -05:00.

// Get the current date and time upto milliseconds.
int year = time:getYear(time); // E.g., 2018
int month = time:getMonth(time); // E.g., 3
int day = time:getDay(time); // E.g., 1
int hour = time:getHour(time); // E.g., 18
int minute = time:getMinute(time); // E.g., 56
int second = time:getSecond(time); // E.g., 23
int milliSecond = time:getMilliSecond(time); // E.g., 555
string weekday = time:getWeekday(time); // Day of the week. E.g., “TUESDAY”.
```

### Creating a time/date record

```ballerina
// Create a record of type ‘Time’ with time zone.
time:TimeZone zoneIdValue = {id:"America/Panama"};
time:Time time1 = {time: 1498488382000, zone: zoneIdValue};

// Create a record of type ‘Time’ with the time zone offset.
time:TimeZone zoneOffsetValue = {id:"-05:00"};
time:Time time2 = {time: 1498488382000, zone: zoneOffsetValue};

// Create a record of type ‘Time’ without the time zone.
time:TimeZone noZoneValue = {id:""};
time:Time time3 = {time: 1498488382000, zone: noZoneValue};

// Create a record of type ‘Time’ with time and date. E.g., 2018-03-28T23:42:45.554-05:00
time:Time dateTime = time:createTime(2018, 3, 28, 23, 42, 45, 554, "America/Panama");
```


### Formatting a time/date to a string

```ballerina
time:TimeZone zoneValue = {id:"America/Panama"};
time:Time time = {time: 1498488382444, zone: zoneValue};

//Format a time to a string of a given pattern.
string time1 = time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ"); //E.g., “2017-06-26T09:46:22.444-0500”.

//Format a time to a string of the RFC-1123 format.
string time2 = time:format(time, time:TIME_FORMAT_RFC_1123); // E.g., "Mon, 26 Jun 2017 09:46:22 -0500”

// Convert a time record to a string value.
string time3 = time:toString(time); //”2017-06-26T09:46:22.444-05:00”
```

### Parsing a string to time/date

```ballerina
// Parse a time string of a given format. 
time:Time time1 = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ"); // The ‘Z’ stands for the time zone.

// Parse a time string of the RFC-1123 format.
time:Time time2 = time:parse("Wed, 28 Mar 2018 11:56:23 +0530", time:TIME_FORMAT_RFC_1123);
```

### Setting time durations

```ballerina
// Add a duration to a given time.
time:Time time1 = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
time1 = time:addDuration(time1, 1, 1, 1, 1, 1, 1, 1); // Adds 1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second, and 1 millisecond.

// Subtract a duration from a given time.
time:Time time2 = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
time2 = time:subtractDuration(time2, 1, 1, 1, 1, 1, 1, 1);  // Subtracts 1 year, 1 month, 1 day, 1 hour, 1 minute, 1 second, and 1 millisecond.

// Get the duration between two times.
int diffInMillis = time1.time - time2.time;
```
