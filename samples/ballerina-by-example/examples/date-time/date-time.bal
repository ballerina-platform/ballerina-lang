function main (string[] args) {
    //Time struct can be created by currentTime, createTime or parse functions.
    //Get the current time.
    Time time = currentTime();
    int currentTimeMills = time.time;
    println("Current system time in milliseconds: "
            + currentTimeMills);
    //Create a time with the required year, month ,date,
    //time and zone information.
    Time timeCreated = createTime(2017, 3, 28, 23, 42, 45, 554,
                                  "America/Panama");
    println("Created Time: " + timeCreated.toString());
    //Returns the time for the given string representation
    //based on the given format string.
    Time timeParsed = parse("2017-06-26T09:46:22.444-0500",
                            "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    println("Parsed Time: " + timeParsed.toString());
    //String representation of time can be retrieved via toString or format
    //functions.Get the ISO 8601 formatted string of a given time.
    string standardTimeString = time.toString();
    println("Current system time in ISO format: " + standardTimeString);
    //Get the formatted string of a given time.
    string customTimeString = time.format("yyyy-MM-dd-E");
    println("Current system time in custom format: " + customTimeString);
    //Information of time can be retrieved via following
    //functions.Get the year of a given time.
    int year = time.year();
    println("Year: " + year);
    //Get the month of a given time.
    int month = time.month();
    println("Month: " + month);
    //Get the day of a given time.
    int day = time.day();
    println("Day: " + day);
    //Get the hour of a given time.
    int hour = time.hour();
    println("Hour: " + hour);
    //Get the minute of a given time.
    int minute = time.minute();
    println("Minute: " + minute);
    //Get the second of a given time.
    int second = time.second();
    println("Second: " + second);
    //Get the milli second of a given time.
    int milliSecond = time.milliSecond();
    println("Millisecond: " + milliSecond);
    //Get the week day of a given time.
    string weekday = time.weekday();
    println("Weekday: " + weekday);
    //Get date components of time using a single function.
    year, month, day = time.getDate();
    println("Date: " + year + ":" + month + ":" + day);
    //Get time components using a single function.
    hour, minute, second, milliSecond = time.getTime();
    println("Time:" + hour + ":" + minute + ":" + second + ":"
            + milliSecond);
    //Add a given duration to the time. Here we are adding
    //one year, one month and one second to the current time.
    Time tmAdd = time.addDuration(1, 1, 0, 0, 0, 1, 0);
    println("After add duration: " + tmAdd.toString());
    //Subtract a given duration from the time. Here we are subtracting one year,
    //one month and one second from the current time.
    Time tmSub = time.subtractDuration(1, 1, 0, 0, 0, 1, 0);
    println("After subtract duration: " + tmSub.toString());
    //Convert to a different time zone.
    Time t1 = createTime(2017, 3, 28, 23, 42, 45, 554,
                         "America/Panama");
    println("Before convert zone: " + t1.toString());
    Time t2 = t1.toTimezone("Asia/Colombo");
    println("After convert zone:" + t2.toString());
}
