import ballerina/time;

function testCurrentTime () returns (int, string, int){
    time:Time time = time:currentTime();
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testNanoTime() returns (int) {
    int nanoTime = time:nanoTime();
    return nanoTime;
}

function testCreateTimeWithZoneID () returns (int, string, int) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1498488382000, zoneValue);
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateTimeWithOffset () returns (int, string, int) {
    time:Timezone zoneValue = {zoneId:"-05:00"};
    time:Time time =new (1498488382000, zoneValue);
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateTimeWithNoZone () returns (int, string, int) {
    time:Timezone zoneValue = {zoneId:""};
    time:Time time = new (1498488382000, zoneValue);
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateDateTime () returns (string) {
    time:Time time = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    return time.toString();
}


function testParseTime () returns (int, string, int) {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testParseRFC1123Time (string timestamp) returns (int, string, int) {
    time:Time time = time:parseTo(timestamp, time:TIME_FORMAT_RFC_1123);
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testToStringWithCreateTime () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1498488382000, zoneValue);
    return time.toString();
}

function testFormatTime () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1498488382444, zoneValue);
    return time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testFormatTimeToRFC1123 () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1498488382444, zoneValue);
    return time.formatTo(time:TIME_FORMAT_RFC_1123);
}

function testGetFunctions () returns (int, int, int, int, int, int, int, string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1456876583555, zoneValue);
    int year = time.year();
    int month = time.month();
    int day = time.day();
    int hour = time.hour();
    int minute = time.minute();
    int second = time.second();
    int milliSecond = time.milliSecond();
    string weekday = time.weekday();
    return (year, month, day, hour, minute, second, milliSecond, weekday);
}

function testGetDateFunction () returns (int, int, int) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1456876583555, zoneValue);
    int year; int month; int day;
    (year, month, day) = time.getDate();
    return (year, month, day);
}

function testGetTimeFunction () returns (int, int, int, int) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1456876583555, zoneValue);
    int hour; int minute; int second; int milliSecond;
    (hour, minute, second, milliSecond) = time.getTime();
    return (hour, minute, second, milliSecond);
}

function testAddDuration () returns (string) {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time.addDuration(1, 1, 1, 1, 1, 1, 1);
    return time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testSubtractDuration () returns (string) {
    time:Time time = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time.subtractDuration(1, 1, 1, 1, 1, 1, 1);
    return time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testToTimezone () returns (string, string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1456876583555, zoneValue);
    string timeStrBefore = time.toString();
    time = time.toTimezone("Asia/Colombo");
    string timeStrAfter = time.toString();
    return (timeStrBefore, timeStrAfter);
}

function testToTimezoneWithInvalidZone () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1456876583555, zoneValue);
    time = time.toTimezone("test");
    return time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testToTimezoneWithDateTime () returns (string) {
    time:Time time = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time.toTimezone("Asia/Colombo");
    return time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testManualTimeCreate () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1498488382000, zoneValue);
    return time.toString();
}

function testManualTimeCreateWithNoZone () returns (int) {
    time:Timezone zoneValue = {zoneId: ""};
    time:Time time = new (1498488382555, zoneValue);
    return time.year();
}

function testManualTimeCreateWithEmptyZone () returns (int) {
    time:Timezone zoneValue = {zoneId:""};
    time:Time time = new (1498488382555, zoneValue);
    return time.year();
}

function testManualTimeCreateWithInvalidZone () returns (int) {
    time:Timezone zoneValue = {zoneId:"test"};
    time:Time time = new (1498488382555, zoneValue);
    return time.year();
}

function testParseTimenvalidPattern () returns (int, string, int) {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "test");
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testParseTimenFormatMismatch () returns (int, string, int) {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd");
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testFormatTimeInvalidPattern () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new (1498488382444, zoneValue);
    return time.format("test");
}

function testParseTimeWithDifferentFormats () returns (int, int, int, int, int, int, int, string, string, string,
                                                       string) {
    time:Time time = time:parse("2017", "yyyy");
    int year = time.year();
    time = time:parse("03", "MM");
    int month = time.month();
    time = time:parse("31", "dd");
    int day = time.day();
    time = time:parse("16", "HH");
    int hour = time.hour();
    time = time:parse("59", "mm");
    int minute = time.minute();
    time = time:parse("58", "ss");
    int second = time.second();
    time = time:parse("999", "SSS");
    int milliSecond = time.milliSecond();
    time = time:parse("2017/09/23", "yyyy/MM/dd");
    string dateStr = time.format("yyyy-MM-dd");
    time = time:parse("2015/02/15+0800", "yyyy/MM/ddZ");
    string dateZoneStr = time.format("yyyy-MM-ddZ");
    time = time:parse("08/23/59.544+0700", "HH/mm/ss.SSSZ");
    string timeZoneStr = time.format("HH-mm-ss-SSS:Z");
    time = time:parse("2014/05/29-23:44:59.544", "yyyy/MM/dd-HH:mm:ss.SSS");
    string datetimeStr = time.format("yyyy-MM-dd-HH:mm:ss.SSS");
    return (year, month, day, hour, minute, second, milliSecond, dateStr, dateZoneStr, timeZoneStr, datetimeStr);
}
