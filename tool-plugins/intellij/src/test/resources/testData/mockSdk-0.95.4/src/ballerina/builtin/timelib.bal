
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
public native function currentTime()(Time);

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
public native function createTime(int year, int month, int date, int hour, int minute, int second, int milliSecond,
                                  string zoneId)(Time);

@Description {value:"Returns the time for the given string representation based on the given format string."}
@Param {value:"data: The time text to parse"}
@Param {value:"format: The format which is used to parse the given text"}
@Return { value:"Time struct containing time and zone information."}
public native function parse(string data, string format) (Time);

@Description {value:"Returns ISO 8601 string representation of the given time."}
@Param {value:"time: The time struct for which needs to get the string representation"}
@Return { value:"The ISO 8601 formatted string of the given time."}
public native function <Time time> toString()(string);

@Description {value:"Returns formatted string representation of the given time."}
@Param {value:"time: The time struct for which needs to get the string representation"}
@Param {value:"format: The format which is used to format the given text"}
@Return { value:"The formatted string of the given time."}
public native function <Time time> format(string format)(string);

@Description {value:"Returns the year representation of the given time."}
@Param {value:"time: The time struct which needs to get the year representation"}
@Return { value:"The year representation."}
public native function <Time time> year()(int);

@Description {value:"Returns the month representation of the given time."}
@Param {value:"time: The time struct which needs to get the month representation"}
@Return { value:"The month-of-year, from 1 (January) to 12 (December)."}
public native function <Time time> month()(int);

@Description {value:"Returns the date representation of the given time."}
@Param {value:"time: The time struct which needs to get the date representation"}
@Return { value:"The day-of-month, from 1 to 31."}
public native function <Time time> day()(int);

@Description {value:"Returns the weekday representation of the given time."}
@Param {value:"time: The time struct which needs to get the weekday representation"}
@Return { value:"The weekday representation from SUNDAY to SATURDAY."}
public native function <Time time> weekday()(string);

@Description {value:"Returns the hour representation of the given time."}
@Param {value:"time: The time struct which needs to get the hour representation"}
@Return { value:"The hour-of-day, from 0 to 23."}
public native function <Time time> hour()(int);

@Description {value:"Returns the minute representation of the given time."}
@Param {value:"time: The time struct which needs to get the minute representation"}
@Return { value:"The minute-of-hour to represent, from 0 to 59."}
public native function <Time time> minute()(int);

@Description {value:"Returns the second representation of the given time."}
@Param {value:"time: The time struct which needs to get the second representation"}
@Return { value:"The second-of-minute, from 0 to 59."}
public native function <Time time> second()(int);

@Description {value:"Returns the millisecond representation of the given time."}
@Param {value:"time: The time struct which needs to get the millisecond representation"}
@Return { value:"The milli-of-second, from 0 to 999."}
public native function <Time time> milliSecond()(int);

@Description {value:"Returns the date representation of the given time."}
@Param {value:"time: The time struct which needs to get the date representation"}
@Return { value:"The year representation."}
@Return { value:"The month-of-year, from 1 (January) to 12 (December)."}
@Return { value:"The day-of-month, from 1 to 31."}
public native function <Time time> getDate()(int, int, int);

@Description {value:"Returns the time representation of the given time."}
@Param {value:"time: The time struct which needs to get the time representation"}
@Return {value:"The hour-of-day, from 0 to 23."}
@Return {value:"The minute-of-hour to represent, from 0 to 59."}
@Return {value:"The second-of-minute, from 0 to 59."}
@Return {value:"The milli-of-second, from 0 to 999."}
public native function <Time time> getTime()(int, int, int, int);

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
public native function <Time time> addDuration(int years, int months, int days, int hours, int minutes, int seconds,
    int milliSeconds)(Time);

@Description {value:"Subtract specified durations from the given time value."}
@Param {value:"years: The year representation"}
@Param {value:"months: The month-of-year to represent, from 1 (January) to 12 (December)"}
@Param {value:"days: The day-of-month to represent, from 1 to 31"}
@Param {value:"hours: The hour-of-day to represent, from 0 to 23"}
@Param {value:"minutes: The minute-of-hour to represent, from 0 to 59"}
@Param {value:"seconds: The second-of-minute to represent, from 0 to 59"}
@Param {value:"milliSeconds: The milli-of-second to represent, from 0 to 999"}
@Return { value:"Time struct containing time and zone information after the subtraction."}
public native function <Time time> subtractDuration(int years, int months, int days, int hours, int minutes, int seconds,
    int milliSeconds)(Time);

@Description {value:"Change the timezone of the given time."}
@Param {value:"time: The time struct which needs to change the timezone information"}
@Param {value:"zoneId: The new timezone id"}
@Return { value:"Time struct containing time and zone information after the conversion."}
public native function <Time time> toTimezone(string zoneId)(Time);
