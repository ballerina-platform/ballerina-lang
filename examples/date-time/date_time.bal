import ballerina/io;
import ballerina/time;

public function main() {
    // To create the `time:Time` object you can use either the `currentTime()`, `createTime()`, or the `parse()` function.
    // This fetches the current time.
    time:Time time = time:currentTime();
    int currentTimeMills = time.time;
    io:println("Current system time in milliseconds: " + currentTimeMills);
    // Specify a time with the required year, month, date,
    // time, and timezone information.
    time:Time timeCreated = time:createTime(2017, 3, 28, 23, 42, 45, 554,
                                            "America/Panama");
    io:println("Created Time: " + time:toString(timeCreated));
    // This retrieves the time for a given string representation
    // based on the specified string format.
    time:Time t1 = time:parse("2017-06-26T09:46:22.444-0500",
                              "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    io:println("Parsed Time: " + time:toString(t1));
    // You can retrieve the string representation of the time via the `toString()` function or the `format()` function.
    // This fetches the ISO 8601 formatted string of a given time.
    string standardTimeString = time:toString(time);
    io:println("Current system time in ISO format: " + standardTimeString);
    // This fetches the formatted string of a given time.
    string customTimeString = time:format(time, "yyyy-MM-dd-E");
    io:println("Current system time in custom format: " + customTimeString);
    // These functions retrieve information relating to a time object.
    // This fetches the year of a given time.
    int year = time:getYear(time);
    io:println("Year: " + year);
    // This fetches the month of a given time.
    int month = time:getMonth(time);
    io:println("Month: " + month);
    // This fetches the day of a given time.
    int day = time:getDay(time);
    io:println("Day: " + day);
    // This fetches the hour value of a given time.
    int hour = time:getHour(time);
    io:println("Hour: " + hour);
    // This fetches the minute value of a given time.
    int minute = time:getMinute(time);
    io:println("Minute: " + minute);
    // This fetches the seconds value of a given time.
    int second = time:getSecond(time);
    io:println("Second: " + second);
    // This fetches the millisecond value of a given time.
    int milliSecond = time:getMilliSecond(time);
    io:println("Millisecond: " + milliSecond);
    // This fetches the day of the week of a given time.
    string weekday = time:getWeekday(time);
    io:println("Weekday: " + weekday);
    // This fetches the date component of time using a single function.
    (year, month, day) = time:getDate(time);
    io:println("Date: " + year + ":" + month + ":" + day);
    // This fetches the time component using a single function.
    (hour, minute, second, milliSecond) = time:getTime(time);
    io:println("Time:" + hour + ":" + minute + ":" + second + ":"
                    + milliSecond);
    // This adds a given duration to a time. In this example, let's add
    // one year, one month, and one second to the current time.
    time:Time tmAdd = time:addDuration(time, 1, 1, 0, 0, 0, 1, 0);
    io:println("After adding a duration: " + time:toString(tmAdd));
    // This subtracts a given duration from a time. In this example, let's subtract one year,
    // one month, and one second from the current time.
    time:Time tmSub = time:subtractDuration(time, 1, 1, 0, 0, 0, 1, 0);
    io:println("After subtracting a duration: " + time:toString(tmSub));
    // This converts the time to a different timezone.
    time:Time t2 = time:createTime(2017, 3, 28, 23, 42, 45, 554,
                                    "America/Panama");
    io:println("Before converting the time zone: " + time:toString(t2));
    time:Time t3 = time:toTimeZone(t2, "Asia/Colombo");
    io:println("After converting the time zone:" + time:toString(t3));
}
