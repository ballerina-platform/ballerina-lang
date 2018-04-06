import ballerina/io;
import ballerina/time;

function main (string[] args) {
    // Time struct can be created by currentTime, createTime or parse functions.
    // Get the current time.
    time:Time time = time:currentTime();
    int currentTimeMills = time.time;
    io:println("Current system time in milliseconds: " + currentTimeMills);
    // Create a time with the required year, month, date,
    // time, and timezone information.
    time:Time timeCreated = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    io:println("Created Time: " + timeCreated.toString());
    // Returns the time for the given string representation
    // based on the given format string.
    time:Time t1 = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    io:println("Parsed Time: " + t1.toString());
    // String representation of time can be retrieved via toString or format
    // functions. Get the ISO 8601 formatted string of a given time.
    string standardTimeString = time.toString();
    io:println("Current system time in ISO format: " + standardTimeString);
    // Get the formatted string of a given time.
    string customTimeString = time.format("yyyy-MM-dd-E");
    io:println("Current system time in custom format: " + customTimeString);
    // Information of time can be retrieved via the following
    // functions. Get the year of a given time.
    int year = time.year();
    io:println("Year: " + year);
    // Get the month of a given time.
    int month = time.month();
    io:println("Month: " + month);
    // Get the day of a given time.
    int day = time.day();
    io:println("Day: " + day);
    // Get the hour value of a given time.
    int hour = time.hour();
    io:println("Hour: " + hour);
    // Get the minute value of a given time.
    int minute = time.minute();
    io:println("Minute: " + minute);
    // Get the seconds value of a given time.
    int second = time.second();
    io:println("Second: " + second);
    // Get the millisecond value of a given time.
    int milliSecond = time.milliSecond();
    io:println("Millisecond: " + milliSecond);
    // Get the nanosecond value of a given time.
    int nanoSecond = time.nanoSecond();
    io:println("Nanosecond: " + nanoSecond);
    // Get the weekday value of a given time.
    string weekday = time.weekday();
    io:println("Weekday: " + weekday);
    // Get date components of time using a single function.
    (year, month, day) = time.getDate();
    io:println("Date: " + year + ":" + month + ":" + day);
    // Get time components using a single function.
    (hour, minute, second, milliSecond) = time.getTime();
    io:println("Time:" + hour + ":" + minute + ":" + second + ":" + milliSecond);
    // Add a given duration to the time. Here we are adding
    // one year, one month, and one second to the current time.
    time:Time tmAdd = time.addDuration(1, 1, 0, 0, 0, 1, 0);
    io:println("After add duration: " + tmAdd.toString());
    // Subtract a given duration from the time. Here we are subtracting one year,
    // one month, and one second from the current time.
    time:Time tmSub = time.subtractDuration(1, 1, 0, 0, 0, 1, 0);
    io:println("After subtract duration: " + tmSub.toString());
    // Convert to a different timezone.
    time:Time t2 = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    io:println("Before convert zone: " + t2.toString());
    time:Time t3 = t2.toTimezone("Asia/Colombo");
    io:println("After convert zone:" + t3.toString());
}
