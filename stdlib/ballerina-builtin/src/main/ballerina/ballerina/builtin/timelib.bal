package ballerina.builtin;

@Description {value:"Ballerina Timezone struct represents the timezone information associated with a particular time."}
@Field {value:"zoneId: Zone short ID or offset string."}
@Field {value:"zoneOffset: The offset in seconds."}
public struct Timezone {
    string zoneId;
    int zoneOffset;
}

@Description {value:"Ballerina Time struct represents a particular time with its associated timezone."}
@Field {value:"time: Time value as milliseconds since epoch."}
@Field {value:"zone: The time   zone of the time."}
public struct Time {
    int time;
    Timezone zone;
}

@Description {value:"Returns the current time value with the system default timezone."}
@Return { value:"Time struct containing the time and zone information."}
documentation {
Returns the current time value with the system default timezone.
- #time Time struct containing the time and zone information.
}
public native function currentTime()(Time time);

@Description {value:"Returns the Time struct correspoding to the given time components and timezone."}
@Param {value:"year: The year representation"}
@Param {value:"month: The month-of-year to represent, from 1 (January) to 12 (December)"}
@Param {value:"date: The day-of-month to represent, from 1 to 31"}
@Param {value:"hour: The hour-of-day to represent, from 0 to 23"}
@Param {value:"minute: The minute-of-hour to represent, from 0 to 59"}
@Param {value:"second: The second-of-minute to represent, from 0 to 59"}
@Param {value:"milliSecond: The milli-of-second to represent, from 0 to 999"}
@Param {value:"zoneId: The zone id of the required timezone.If empty the system local timezone will be used"}
@Return { value:"Time struct containing time and zone information."}
documentation {
Returns the Time struct correspoding to the given time components and timezone.
- #year The year representation
- #month The month-of-year to represent, from 1 (January) to 12 (December)
- #date The day-of-month to represent, from 1 to 31
- #hour The hour-of-day to represent, from 0 to 23
- #minute The minute-of-hour to represent, from 0 to 59
- #second The second-of-minute to represent, from 0 to 59
- #millisecond The milli-of-second to represent, from 0 to 999
- #zoneId The zone id of the required timezone.If empty the system local timezone will be used
- #time Time struct containing the time and zone information.
}
public native function createTime(int year, int month, int date, int hour, int minute, int second, int milliSecond,
                                  string zoneId)(Time time);

@Description {value:"Returns the time for the given string representation based on the given format string."}
@Param {value:"data: The time text to parse"}
@Param {value:"format: The format which is used to parse the given text"}
@Return { value:"Time struct containing time and zone information."}
documentation {
Returns the time for the given string representation based on the given format string.
- #data The time text to parse
- #format The format which is used to parse the given text
- #time Time struct containing the time and zone information.
}
public native function parse(string data, string format) (Time time);

@Description {value:"Returns ISO 8601 string representation of the given time."}
@Param {value:"time: The time struct for which needs to get the string representation"}
@Return { value:"The ISO 8601 formatted string of the given time."}
documentation {
Returns ISO 8601 string representation of the given time.
- #time The time struct for which needs to get the string representation
- #s The ISO 8601 formatted string of the given time
}
public native function <Time time> toString()(string s);

@Description {value:"Returns formatted string representation of the given time."}
@Param {value:"time: The time struct for which needs to get the string representation"}
@Param {value:"format: The format which is used to format the given text"}
@Return { value:"The formatted string of the given time."}
documentation {
Returns formatted string representation of the given time.
- #time The time struct for which needs to get the string representation
- #format The format which is used to format the given text
- #s The formatted string of the given time.
}
public native function <Time time> format(string format)(string s);

@Description {value:"Returns the year representation of the given time."}
@Param {value:"time: The time struct which needs to get the year representation"}
@Return { value:"The year representation."}
documentation {
Returns the year representation of the given time.
- #time The time struct for which needs to get the year representation
- #y The year representation
}
public native function <Time time> year()(int y);

@Description {value:"Returns the month representation of the given time."}
@Param {value:"time: The time struct which needs to get the month representation"}
@Return { value:"The month-of-year, from 1 (January) to 12 (December)."}
documentation {
Returns the month representation of the given time.
- #time The time struct for which needs to get the month representation
- #m The month-of-year, from 1 (January) to 12 (December)
}
public native function <Time time> month()(int m);

@Description {value:"Returns the date representation of the given time."}
@Param {value:"time: The time struct which needs to get the date representation"}
@Return { value:"The day-of-month, from 1 to 31."}
documentation {
Returns the date representation of the given time.
- #time The time struct for which needs to get the date representation
- #d The day-of-month, from 1 to 31
}
public native function <Time time> day()(int d);

@Description {value:"Returns the weekday representation of the given time."}
@Param {value:"time: The time struct which needs to get the weekday representation"}
@Return { value:"The weekday representation from SUNDAY to SATURDAY."}
documentation {
Returns the weekday representation of the given time.
- #time The time struct for which needs to get the weekday representation
- #day The weekday representation from SUNDAY to SATURDAY
}
public native function <Time time> weekday()(string day);

@Description {value:"Returns the hour representation of the given time."}
@Param {value:"time: The time struct which needs to get the hour representation"}
@Return { value:"The hour-of-day, from 0 to 23."}
documentation {
Returns the hour representation of the given time.
- #time The time struct for which needs to get the hour representation
- #h The hour-of-day, from 0 to 23.
}
public native function <Time time> hour()(int h);

@Description {value:"Returns the minute representation of the given time."}
@Param {value:"time: The time struct which needs to get the minute representation"}
@Return { value:"The minute-of-hour to represent, from 0 to 59."}
documentation {
Returns the minute representation of the given time.
- #time The time struct for which needs to get the minute representation
- #m The minute-of-hour to represent, from 0 to 59.
}
public native function <Time time> minute()(int m);

@Description {value:"Returns the second representation of the given time."}
@Param {value:"time: The time struct which needs to get the second representation"}
@Return { value:"The second-of-minute, from 0 to 59."}
documentation {
Returns the second representation of the given time.
- #time The time struct for which needs to get the second representation
- #s The second-of-minute, from 0 to 59.
}
public native function <Time time> second()(int s);

@Description {value:"Returns the millisecond representation of the given time."}
@Param {value:"time: The time struct which needs to get the millisecond representation"}
@Return { value:"The milli-of-second, from 0 to 999."}
documentation {
Returns the millisecond representation of the given time.
- #time The time struct for which needs to get the millisecond representation
- #ms The milli-of-second, from 0 to 999.
}
public native function <Time time> milliSecond()(int ms);

@Description {value:"Returns the date representation of the given time."}
@Param {value:"time: The time struct which needs to get the date representation"}
@Return { value:"The year representation."}
@Return { value:"The month-of-year, from 1 (January) to 12 (December)."}
@Return { value:"The day-of-month, from 1 to 31."}
documentation {
Returns the date representation of the given time.
- #time The time struct for which needs to get the date representation
- #y The year representation
- #m The month-of-year, from 1 (January) to 12 (December)
- #d The day-of-month, from 1 to 31
}
public native function <Time time> getDate()(int y, int m, int d);

@Description {value:"Returns the time representation of the given time."}
@Param {value:"time: The time struct which needs to get the time representation"}
@Return {value:"The hour-of-day, from 0 to 23."}
@Return {value:"The minute-of-hour to represent, from 0 to 59."}
@Return {value:"The second-of-minute, from 0 to 59."}
@Return {value:"The milli-of-second, from 0 to 999."}
documentation {
Returns the time representation of the given time.
- #time The time struct for which needs to get the year representation
- #h The hour-of-day, from 0 to 23
- #m The minute-of-hour to represent, from 0 to 59
- #s The second-of-minute, from 0 to 59
- #ms The milli-of-second, from 0 to 999
}
public native function <Time time> getTime()(int h, int m, int s, int ms);

@Description {value:"Add specified durations to the given time value."}
@Param {value:"time: The time struct which needs to add the given duration"}
@Param {value:"years: The year representation"}
@Param {value:"months: The month-of-year to represent, from 1 (January) to 12 (December)"}
@Param {value:"days: The day-of-month to represent, from 1 to 31"}
@Param {value:"hours: The hour-of-day to represent, from 0 to 23"}
@Param {value:"minutes: The minute-of-hour to represent, from 0 to 59"}
@Param {value:"seconds: The second-of-minute to represent, from 0 to 59"}
@Param {value:"milliSeconds: The milli-of-second to represent, from 0 to 999"}
@Return { value:"Time struct containing time and zone information after the addition."}
documentation {
Add specified durations to the given time value.
- #time The time struct for which needs to get the year representation
- #years The year representation
- #months The month-of-year, from 1 (January) to 12 (December)
- #days The day-of-month to represent, from 1 to 31
- #hours The hour-of-day to represent, from 0 to 23
- #minutes The minute-of-hour to represent, from 0 to 59
- #seconds The second-of-minute to represent, from 0 to 59
- #milliSeconds The milli-of-second to represent, from 0 to 999
- #t Time struct containing time and zone information after the addition
}
public native function <Time time> addDuration(int years, int months, int days, int hours, int minutes, int seconds,
    int milliSeconds)(Time t);

@Description {value:"Subtract specified durations from the given time value."}
@Param {value:"years: The year representation"}
@Param {value:"months: The month-of-year to represent, from 1 (January) to 12 (December)"}
@Param {value:"days: The day-of-month to represent, from 1 to 31"}
@Param {value:"hours: The hour-of-day to represent, from 0 to 23"}
@Param {value:"minutes: The minute-of-hour to represent, from 0 to 59"}
@Param {value:"seconds: The second-of-minute to represent, from 0 to 59"}
@Param {value:"milliSeconds: The milli-of-second to represent, from 0 to 999"}
@Return { value:"Time struct containing time and zone information after the subtraction."}
documentation {
Subtract specified durations from the given time value.
- #time The time struct for which needs to get the year representation
- #years The year representation
- #months The month-of-year, from 1 (January) to 12 (December)
- #days The day-of-month to represent, from 1 to 31
- #hours The hour-of-day to represent, from 0 to 23
- #minutes The minute-of-hour to represent, from 0 to 59
- #seconds The second-of-minute to represent, from 0 to 59
- #milliSeconds The milli-of-second to represent, from 0 to 999
- #t Time struct containing time and zone information after the addition
}
public native function <Time time> subtractDuration(int years, int months, int days, int hours, int minutes, int seconds,
    int milliSeconds)(Time t);

@Description {value:"Change the timezone of the given time."}
@Param {value:"time: The time struct which needs to change the timezone information"}
@Param {value:"zoneId: The new timezone id"}
@Return { value:"Time struct containing time and zone information after the conversion."}
documentation {
Change the timezone of the given time.
- #time The time struct which needs to change the timezone information
- #zoneId The new timezone id
- #t Time struct containing time and zone information after the addition
}
public native function <Time time> toTimezone(string zoneId)(Time t);
