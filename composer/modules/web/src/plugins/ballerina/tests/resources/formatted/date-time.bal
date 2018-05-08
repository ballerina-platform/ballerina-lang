import ballerina/lang.system;
import ballerina/lang.time;

function main (string... args) {
    //Time struct can be created by currentTime, createTime or parse functions.
    //Get the current time
    time:Time currentTime = time:currentTime();
    int currentTimeMills = currentTime.time;
    system:println("Current system time in milliseconds since epoch:"
                   + currentTimeMills);
    //Create a time with the required year, month ,date,
    //time and zone information.
    time:Time timeCreated = time:createTime
                            (2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    system:println("Created Time:" + time:toString(timeCreated));
    //Returns the time for the given string representation
    //based on the given format string
    time:Time timeParsed = time:parse("2017-06-26T09:46:22.444-0500",
                                      "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    system:println("Parsed Time:" + time:toString(timeParsed));
    //String representation of time can be retrieved via toString or format
    //functions.Get the ISO 8601 formatted string of a given time
    string standardTimeString = time:toString(currentTime);
    system:println("Current system time in iso format:" + standardTimeString);
    //Get the formatted string of a given time
    string customeTimeString = time:format(currentTime, "yyyy-MM-dd-E");
    system:println("Current system time in custom format:" + customeTimeString);
    //Information of time can be retrieved via following
    //functions.Get the year of a given time.
    int year = time:year(currentTime);
    system:println("Year:" + year);
    //Get the month of a given time.
    int month = time:month(currentTime);
    system:println("Month:" + month);
    //Get the day of a given time.
    int day = time:day(currentTime);
    system:println("Day:" + day);
    //Get the hour of a given time.
    int hour = time:hour(currentTime);
    system:println("Hour:" + hour);
    //Get the minute of a given time.
    int minute = time:minute(currentTime);
    system:println("Minute:" + minute);
    //Get the second of a given time.
    int second = time:second(currentTime);
    system:println("Second:" + second);
    //Get the milli second of a given time.
    int milliSecond = time:milliSecond(currentTime);
    system:println("Millisecond:" + milliSecond);
    //Get the weed day of a given time.
    string weekday = time:weekday(currentTime);
    system:println("Weekday:" + weekday);
    //Get date components of time using a single function
    year, month, day = time:getDate(currentTime);
    system:println("Date:" + year + ":" + month + ":" + day);
    //Get time components using a single function
    hour, minute, second, milliSecond = time:getTime(currentTime);
    system:println("Time:" + hour + ":" + minute + ":" + second + ":"
                   + milliSecond);
    //Add a given duration to the time. Here we are adding
    //one year, one month and one second to the current time
    time:Time tmAdd = time:addDuration(currentTime, 1, 1, 0, 0, 0, 1, 0);
    system:println("After add duration:" + time:toString(tmAdd));
    //Subtract a given duration from the time. Here we are subtracting one year,
    //one month and one second from the current time.
    time:Time tmSub = time:subtractDuration(currentTime, 1, 1, 0, 0, 0, 1, 0);
    system:println("After subtract duration:" + time:toString(tmSub));
    //Convert
    time:Time t1 = time:createTime(2017, 3, 28, 23, 42, 45, 554,
                                   "America/Panama");
    system:println("Before convert zone:" + time:toString(t1));
    time:Time t2 = time:toTimezone(t1, "Asia/Colombo");
    system:println("After convert zone:" + time:toString(t2));
}
