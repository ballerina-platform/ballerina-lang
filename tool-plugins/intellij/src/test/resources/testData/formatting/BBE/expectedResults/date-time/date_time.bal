import ballerina/io;
import ballerina/time;

function main(string... args) {
    // To create the time object you can use either the 'currentTime', 'createTime', or the 'parse' function.
    // This fetches the current time.
    time:Time time = time:currentTime();
    int currentTimeMills = time.time;
    io:println("Current system time in milliseconds: " + currentTimeMills);
    // Specify a time with the required year, month, date,
    // time, and timezone information.
    time:Time timeCreated = time:createTime(2017, 3, 28, 23, 42, 45, 554,
        "America/Panama");
    io:println("Created Time: " + timeCreated.toString());
    // This retrieves the time for a given string representation
    // based on the specified string format.
    time:Time t1 = time:parse("2017-06-26T09:46:22.444-0500",
        "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    io:println("Parsed Time: " + t1.toString());
    // You can retrieve the string representation of the time via the 'toString' or the 'format' function.
    // This fetches the ISO 8601 formatted string of a given time.
    string standardTimeString = time.toString();
    io:println("Current system time in ISO format: " + standardTimeString);
    // This fetches the formatted string of a given time.
    string customTimeString = time.format("yyyy-MM-dd-E");
    io:println("Current system time in custom format: " + customTimeString);
    // These functions retrieve information relating to a time object.
    // This fetches the year of a given time.
    int year = time.year();
    io:println("Year: " + year);
    // This fetches the month of a given time.
    int month = time.month();
    io:println("Month: " + month);
    // This fetches the day of a given time.
    int day = time.day();
    io:println("Day: " + day);
    // This fetches the hour value of a given time.
    int hour = time.hour();
    io:println("Hour: " + hour);
    // This fetches the minute value of a given time.
    int minute = time.minute();
    io:println("Minute: " + minute);
    // This fetches the seconds value of a given time.
    int second = time.second();
    io:println("Second: " + second);
    // This fetches the millisecond value of a given time.
    int milliSecond = time.milliSecond();
    io:println("Millisecond: " + milliSecond);
    // This fetches the day of the week of a given time.
    string weekday = time.weekday();
    io:println("Weekday: " + weekday);
    // This fetches the date component of time using a single function.
    (year, month, day) = time.getDate();
    io:println("Date: " + year + ":" + month + ":" + day);
    // This fetches the time component using a single function.
    (hour, minute, second, milliSecond) = time.getTime();
    io:println("Time:" + hour + ":" + minute + ":" + second + ":" + milliSecond);
    // This adds a given duration to a time. In this example, let's add
    // one year, one month, and one second to the current time.
    time:Time tmAdd = time.addDuration(1, 1, 0, 0, 0, 1, 0);
    io:println("After add duration: " + tmAdd.toString());
    // This subtracts a given duration from a time. In this example, let's subtract one year,
    // one month, and one second from the current time.
    time:Time tmSub = time.subtractDuration(1, 1, 0, 0, 0, 1, 0);
    io:println("After subtract duration: " + tmSub.toString());
    // This converts the time to a different timezone.
    time:Time t2 = time:createTime(2017, 3, 28, 23, 42, 45, 554,
        "America/Panama");
    io:println("Before convert zone: " + t2.toString());
    time:Time t3 = t2.toTimezone("Asia/Colombo");
    io:println("After convert zone:" + t3.toString());
}
