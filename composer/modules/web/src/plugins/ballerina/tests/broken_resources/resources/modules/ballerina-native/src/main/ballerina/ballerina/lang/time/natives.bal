
import ballerina/doc;

@doc:Description {value:"Ballerina Timezone struct represents the timezone information associated with a particular time."}
@doc:Field {value:"zoneId: Zone short ID or offset string."}
@doc:Field {value:"zoneOffset: The offset in seconds."}
struct Timezone {
    string zoneId;
    int zoneOffset;
}

@doc:Description {value:"Ballerina Time struct represents a particular time with its associated timezone."}
@doc:Field {value:"time: Time value as milliseconds since epoch."}
@doc:Field {value:"zone: The time   zone of the time."}
struct Time {
    int time;
    Timezone zone;
}

@doc:Description {value:"Returns the current time value with the system default timezone."}
@doc:Return {value:"ballerina.lang.time:Time: Time struct containing the time and zone information."}
native function currentTime()(Time);

@doc:Description {value:"Returns the the date and time components and timezone."}
@doc:Param {value:"year : The year representation"}
@doc:Param {value:"month : The month-of-year to represent, from 1 (January) to 12 (December)"}
@doc:Param {value:"date : The day-of-month to represent, from 1 to 31"}
@doc:Param {value:"hour : The hour-of-day to represent, from 0 to 23"}
@doc:Param {value:"minute : The minute-of-hour to represent, from 0 to 59"}
@doc:Param {value:"second : The second-of-minute to represent, from 0 to 59"}
@doc:Param {value:"milliSecond : The milli-of-second to represent, from 0 to 999"}
@doc:Param {value:"zoneId : The zone id of the required timezone"}
@doc:Return {value:"ballerina.lang.time:Time: Time struct containing time and zone information."}
native function createTime(int year, int month, int date, int hour, int minute, int second, int milliSecond,
                            string zoneId)(Time);

@doc:Description {value:"Returns the time for the given string representation based on the given format string."}
@doc:Param {value:"data : The time text to parse"}
@doc:Param {value:"format : The format which is used to parse the given text"}
@doc:Return {value:"ballerina.lang.time:Time: Time struct containing time and zone information."}
native function parse(string data, string format) (Time);

@doc:Description {value:"Returns ISO 8601 string representation of the given time."}
@doc:Param {value:"time : The time struct for which needs to get the string representation"}
@doc:Return {value:"string: The ISO 8601 formatted string of the given time."}
native function toString(Time time)(string);

@doc:Description {value:"Returns formatted string representation of the given time."}
@doc:Param {value:"time : The time struct for which needs to get the string representation"}
@doc:Param {value:"format : The format which is used to format the given text"}
@doc:Return {value:"string: The formatted string of the given time."}
native function format(Time time, string format)(string);

@doc:Description {value:"Returns the year representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the year representation"}
@doc:Return {value:"int: The year representation."}
native function year(Time time)(int);

@doc:Description {value:"Returns the month representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the month representation"}
@doc:Return {value:"int: The month-of-year, from 1 (January) to 12 (December)."}
native function month(Time time)(int);

@doc:Description {value:"Returns the date representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the date representation"}
@doc:Return {value:"int: The day-of-month, from 1 to 31."}
native function day(Time time)(int);

@doc:Description {value:"Returns the weekday representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the weekday representation"}
@doc:Return {value:"string: The weekday representation from SUNDAY to SATURDAY."}
native function weekday(Time time)(string);

@doc:Description {value:"Returns the hour representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the hour representation"}
@doc:Return {value:"int: The hour-of-day, from 0 to 23."}
native function hour(Time time)(int);

@doc:Description {value:"Returns the minute representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the minute representation"}
@doc:Return {value:"int: The minute-of-hour to represent, from 0 to 59."}
native function minute(Time time)(int);

@doc:Description {value:"Returns the second representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the second representation"}
@doc:Return {value:"int: The second-of-minute, from 0 to 59."}
native function second(Time time)(int);

@doc:Description {value:"Returns the millisecond representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the millisecond representation"}
@doc:Return {value:"int: The milli-of-second, from 0 to 999."}
native function milliSecond(Time time)(int);

@doc:Description {value:"Returns the date representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the date representation"}
@doc:Return {value:"int: The year representation."}
@doc:Return {value:"int: The month-of-year, from 1 (January) to 12 (December)."}
@doc:Return {value:"int: The day-of-month, from 1 to 31."}
native function getDate(Time time)(int, int, int);

@doc:Description {value:"Returns the time representation of the given time."}
@doc:Param {value:"time : The time struct which needs to get the time representation"}
@doc:Return {value:"int: The hour-of-day, from 0 to 23."}
@doc:Return {value:"int: The minute-of-hour to represent, from 0 to 59."}
@doc:Return {value:"int: The second-of-minute, from 0 to 59."}
@doc:Return {value:"int: The milli-of-second, from 0 to 999."}
native function getTime(Time time)(int, int, int, int);

@doc:Description {value:"Add specified durations to the given time value."}
@doc:Param {value:"year : The year representation"}
@doc:Param {value:"month : The month-of-year to represent, from 1 (January) to 12 (December)"}
@doc:Param {value:"date : The day-of-month to represent, from 1 to 31"}
@doc:Param {value:"hour : The hour-of-day to represent, from 0 to 23"}
@doc:Param {value:"minute : The minute-of-hour to represent, from 0 to 59"}
@doc:Param {value:"second : The second-of-minute to represent, from 0 to 59"}
@doc:Param {value:"milliSecond : The milli-of-second to represent, from 0 to 999"}
@doc:Return {value:"ballerina.lang.time:Time: Time struct containing time and zone information after the addition."}
native function addDuration(Time timeData, int years, int months, int days, int hours, int minutes, int seconds,
                            int milliSeconds)(Time);

@doc:Description {value:"Subtract specified durations from the given time value."}
@doc:Param {value:"year : The year representation"}
@doc:Param {value:"month : The month-of-year to represent, from 1 (January) to 12 (December)"}
@doc:Param {value:"date : The day-of-month to represent, from 1 to 31"}
@doc:Param {value:"hour : The hour-of-day to represent, from 0 to 23"}
@doc:Param {value:"minute : The minute-of-hour to represent, from 0 to 59"}
@doc:Param {value:"second : The second-of-minute to represent, from 0 to 59"}
@doc:Param {value:"milliSecond : The milli-of-second to represent, from 0 to 999"}
@doc:Return {value:"ballerina.lang.time:Time: Time struct containing time and zone information after the subtraction."}
native function subtractDuration(Time timeData, int years, int months, int days, int hours, int minutes, int seconds,
                             int milliSeconds)(Time);

@doc:Description {value:"Change the timezone of the given time."}
@doc:Param {value:"time : The time struct which needs to change the timezone information"}
@doc:Param {value:"format : The new timezone id"}
@doc:Return {value:"ballerina.lang.time:Time: Time struct containing time and zone information after the conversion."}
native function toTimezone(Time timeData, string zoneId)(Time);
