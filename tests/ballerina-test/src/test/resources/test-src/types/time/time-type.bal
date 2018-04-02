import ballerina/time;

function testCurrentTime () returns (int, string, int){
    time:Time timeStruct = time:currentTime();
    int timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateTimeWithZoneID () returns (int, string, int) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382000, zone:zoneValue};
    int timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateTimeWithOffset () returns (int, string, int) {
    time:Timezone zoneValue = {zoneId:"-05:00"};
    time:Time timeStruct = {time:1498488382000, zone:zoneValue};
    int timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateTimeWithNoZone () returns (int, string, int) {
    time:Timezone zoneValue = {zoneId:""};
    time:Time timeStruct = {time:1498488382000, zone:zoneValue};
    int timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testCreateDateTime () returns (string) {
    time:Time timeStruct = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    return timeStruct.toString();
}


function testParseTime () returns (int, string, int) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    int timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testParseRFC1123Time (string timestamp) returns (int, string, int) {
    time:Time timeStruct = time:parseTo(timestamp, time:TimeFormat.RFC_1123);
    int timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testToStringWithCreateTime () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382000, zone:zoneValue};
    return timeStruct.toString();
}

function testFormatTime () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382444, zone:zoneValue};
    return timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testFormatTimeToRFC1123 () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382444, zone:zoneValue};
    return timeStruct.formatTo(time:TimeFormat.RFC_1123);
}

function testGetFunctions () returns (int, int, int, int, int, int, int, string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    int year = timeStruct.year();
    int month = timeStruct.month();
    int day = timeStruct.day();
    int hour = timeStruct.hour();
    int minute = timeStruct.minute();
    int second = timeStruct.second();
    int milliSecond = timeStruct.milliSecond();
    string weekday = timeStruct.weekday();
    return (year, month, day, hour, minute, second, milliSecond, weekday);
}

function testGetDateFunction () returns (int, int, int) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    int year; int month; int day;
    (year, month, day) = timeStruct.getDate();
    return (year, month, day);
}

function testGetTimeFunction () returns (int, int, int, int) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    int hour; int minute; int second; int milliSecond;
    (hour, minute, second, milliSecond) = timeStruct.getTime();
    return (hour, minute, second, milliSecond);
}

function testAddDuration () returns (string) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = timeStruct.addDuration(1, 1, 1, 1, 1, 1, 1);
    return timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testSubtractDuration () returns (string) {
    time:Time timeStruct = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = timeStruct.subtractDuration(1, 1, 1, 1, 1, 1, 1);
    return timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testToTimezone () returns (string, string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    string timeStrBefore = timeStruct.toString();
    timeStruct = timeStruct.toTimezone("Asia/Colombo");
    string timeStrAfter = timeStruct.toString();
    return (timeStrBefore, timeStrAfter);
}

function testToTimezoneWithInvalidZone () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    timeStruct = timeStruct.toTimezone("test");
    return timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testToTimezoneWithDateTime () returns (string) {
    time:Time timeStruct = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = timeStruct.toTimezone("Asia/Colombo");
    return timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

function testManualTimeCreate () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = {time:1498488382000, zone:zoneValue};
    return time.toString();
}

function testManualTimeCreateWithNoZone () returns (int) {
    time:Time time = {time:1498488382555};
    return time.year();
}

function testManualTimeCreateWithEmptyZone () returns (int) {
    time:Timezone zoneValue = {zoneId:""};
    time:Time time = {time:1498488382555, zone: zoneValue};
    return time.year();
}

function testManualTimeCreateWithInvalidZone () returns (int) {
    time:Timezone zoneValue = {zoneId:"test"};
    time:Time time = {time:1498488382555, zone:zoneValue};
    return time.year();
}

function testParseTimenvalidPattern () returns (int, string, int) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "test");
    int timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testParseTimenFormatMismatch () returns (int, string, int) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd");
    int timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return (timeValue, zoneId, zoneoffset);
}

function testFormatTimeInvalidPattern () returns (string) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382444, zone:zoneValue};
    return timeStruct.format("test");
}

function testParseTimeWithDifferentFormats () returns (int, int, int, int, int, int, int, string, string, string,
                                                       string) {
    time:Time timeStruct = time:parse("2017", "yyyy");
    int year = timeStruct.year();
    timeStruct = time:parse("03", "MM");
    int month = timeStruct.month();
    timeStruct = time:parse("31", "dd");
    int day = timeStruct.day();
    timeStruct = time:parse("16", "HH");
    int hour = timeStruct.hour();
    timeStruct = time:parse("59", "mm");
    int minute = timeStruct.minute();
    timeStruct = time:parse("58", "ss");
    int second = timeStruct.second();
    timeStruct = time:parse("999", "SSS");
    int milliSecond = timeStruct.milliSecond();
    timeStruct = time:parse("2017/09/23", "yyyy/MM/dd");
    string dateStr = timeStruct.format("yyyy-MM-dd");
    timeStruct = time:parse("2015/02/15+0800", "yyyy/MM/ddZ");
    string dateZoneStr = timeStruct.format("yyyy-MM-ddZ");
    timeStruct = time:parse("08/23/59.544+0700", "HH/mm/ss.SSSZ");
    string timeZoneStr = timeStruct.format("HH-mm-ss-SSS:Z");
    timeStruct = time:parse("2014/05/29-23:44:59.544", "yyyy/MM/dd-HH:mm:ss.SSS");
    string datetimeStr = timeStruct.format("yyyy-MM-dd-HH:mm:ss.SSS");
    return (year, month, day, hour, minute, second, milliSecond, dateStr, dateZoneStr, timeZoneStr, datetimeStr);
}
