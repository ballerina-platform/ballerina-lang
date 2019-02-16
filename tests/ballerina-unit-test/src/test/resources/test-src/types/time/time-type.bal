import ballerina/time;

function testCurrentTime() returns (int, string, int) {
    time:Time time = time:currentTime();
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return (timeValue, zoneId, zoneoffset);
}

function testNanoTime() returns (int) {
    int nanoTime = time:nanoTime();
    return nanoTime;
}

function testCreateTimeWithZoneID() returns (int, string, int) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateTimeWithOffset() returns (int, string, int) {
    time:TimeZone zoneValue = {id:"-05:00"};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateTimeWithNoZone() returns (int, string, int) {
    time:TimeZone zoneValue = {id:""};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateDateTime() returns (string) {
    time:Time time = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    return time:toString(time);
}


function testParseTime() returns (int, string, int) {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return (timeValue, zoneId, zoneoffset);
}

function testParseRFC1123Time(string timestamp) returns (int, string, int) {
    time:Time time = time:parse(timestamp, time:TIME_FORMAT_RFC_1123);
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return (timeValue, zoneId, zoneoffset);
}

function testToStringWithCreateTime() returns (string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    return time:toString(time);
}

function testFormatTime() returns (string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382444, zone: zoneValue };
    return time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testFormatTimeToRFC1123() returns (string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382444, zone: zoneValue };
    return time:format(time, time:TIME_FORMAT_RFC_1123);
}

function testGetFunctions() returns (int, int, int, int, int, int, int, string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    int year = time:getYear(time);
    int month = time:getMonth(time);
    int day = time:getDay(time);
    int hour = time:getHour(time);
    int minute = time:getMinute(time);
    int second = time:getSecond(time);
    int milliSecond = time:getMilliSecond(time);
    string weekday = time:getWeekday(time);
    return (year, month, day, hour, minute, second, milliSecond, weekday);
}

function testGetDateFunction() returns (int, int, int) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    int year; int month; int day;
    (year, month, day) = time:getDate(time);
    return (year, month, day);
}

function testGetTimeFunction() returns (int, int, int, int) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    int hour; int minute; int second; int milliSecond;
    (hour, minute, second, milliSecond) = time:getTime(time);
    return (hour, minute, second, milliSecond);
}

function testAddDuration() returns (string) {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time:addDuration(time, 1, 1, 1, 1, 1, 1, 1);
    return time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testSubtractDuration() returns (string) {
    time:Time time = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time:subtractDuration(time, 1, 1, 1, 1, 1, 1, 1);
    return time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testToTimezone() returns (string, string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    string timeStrBefore = time:toString(time);
    time = time:toTimeZone(time, "Asia/Colombo");
    string timeStrAfter = time:toString(time);
    return (timeStrBefore, timeStrAfter);
}

function testToTimezoneWithInvalidZone() returns (string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    time = time:toTimeZone(time, "test");
    return time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testToTimezoneWithDateTime() returns (string) {
    time:Time time = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time:toTimeZone(time, "Asia/Colombo");
    return time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testManualTimeCreate() returns (string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    return time:toString(time);
}

function testManualTimeCreateWithNoZone() returns (int) {
    time:TimeZone zoneValue = {id:""};
    time:Time time = { time: 1498488382555, zone: zoneValue };
    return time:getYear(time);
}

function testManualTimeCreateWithEmptyZone() returns (int) {
    time:TimeZone zoneValue = {id:""};
    time:Time time = { time: 1498488382555, zone: zoneValue };
    return time:getYear(time);
}

function testManualTimeCreateWithInvalidZone() returns (int) {
    time:TimeZone zoneValue = {id:"test"};
    time:Time time = { time: 1498488382555, zone: zoneValue };
    return time:getYear(time);
}

function testParseTimenvalidPattern() returns (int, string, int) {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "test");
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return (timeValue, zoneId, zoneoffset);
}

function testParseTimenFormatMismatch() returns (int, string, int) {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd");
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return (timeValue, zoneId, zoneoffset);
}

function testFormatTimeInvalidPattern() returns (string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382444, zone: zoneValue };
    return time:format(time, "test");
}

function testParseTimeWithDifferentFormats() returns (int, int, int, int, int, int, int, string, string, string,
        string) {
    time:Time time = time:parse("2017", "yyyy");
    int year = time:getYear(time);
    time = time:parse("03", "MM");
    int month = time:getMonth(time);
    time = time:parse("31", "dd");
    int day = time:getDay(time);
    time = time:parse("16", "HH");
    int hour = time:getHour(time);
    time = time:parse("59", "mm");
    int minute = time:getMinute(time);
    time = time:parse("58", "ss");
    int second = time:getSecond(time);
    time = time:parse("999", "SSS");
    int milliSecond = time:getMilliSecond(time);
    time = time:parse("2017/09/23", "yyyy/MM/dd");
    string dateStr = time:format(time, "yyyy-MM-dd");
    time = time:parse("2015/02/15+0800", "yyyy/MM/ddZ");
    string dateZoneStr = time:format(time, "yyyy-MM-ddZ");
    time = time:parse("08/23/59.544+0700", "HH/mm/ss.SSSZ");
    string timeZoneStr = time:format(time, "HH-mm-ss-SSS:Z");
    time = time:parse("2014/05/29-23:44:59.544", "yyyy/MM/dd-HH:mm:ss.SSS");
    string datetimeStr = time:format(time, "yyyy-MM-dd-HH:mm:ss.SSS");
    return (year, month, day, hour, minute, second, milliSecond, dateStr, dateZoneStr, timeZoneStr, datetimeStr);
}
