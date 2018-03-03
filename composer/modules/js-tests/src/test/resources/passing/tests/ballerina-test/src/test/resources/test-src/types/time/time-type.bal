import ballerina.time;

function testCurrentTime () (int timeValue, string zoneId, int zoneoffset) {
    time:Time timeStruct = time:currentTime();
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testCreateTimeWithZoneID () (int timeValue, string zoneId, int zoneoffset) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382000, zone:zoneValue};
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testCreateTimeWithOffset () (int timeValue, string zoneId, int zoneoffset) {
    time:Timezone zoneValue = {zoneId:"-05:00"};
    time:Time timeStruct = {time:1498488382000, zone:zoneValue};
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testCreateTimeWithNoZone () (int timeValue, string zoneId, int zoneoffset) {
    time:Timezone zoneValue = {zoneId:""};
    time:Time timeStruct = {time:1498488382000, zone:zoneValue};
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testCreateTimeWithNullZone () (int year) {
    time:Time timeStruct = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    timeStruct.zone = null;
    year = timeStruct.year();
    return;
}

function testCreateDateTime () (string timeString) {
    time:Time timeStruct = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    timeString = timeStruct.toString();
    return;
}


function testParseTime () (int timeValue, string zoneId, int zoneoffset) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testToStringWithCreateTime () (string timeString) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382000, zone:zoneValue};
    timeString = timeStruct.toString();
    return;
}

function testFormatTime () (string timeString) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382444, zone:zoneValue};
    timeString = timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testGetFunctions () (int year, int month, int day, int hour, int minute, int second, int milliSecond,
                              string weekday) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    year = timeStruct.year();
    month = timeStruct.month();
    day = timeStruct.day();
    hour = timeStruct.hour();
    minute = timeStruct.minute();
    second = timeStruct.second();
    milliSecond = timeStruct.milliSecond();
    weekday = timeStruct.weekday();
    return;
}

function testGetDateFunction () (int year, int month, int day) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    year, month, day = timeStruct.getDate();
    return;
}

function testGetTimeFunction () (int hour, int minute, int second, int milliSecond) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    hour, minute, second, milliSecond = timeStruct.getTime();
    return;
}

function testAddDuration () (string timeString) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = timeStruct.addDuration(1, 1, 1, 1, 1, 1, 1);
    timeString = timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testSubtractDuration () (string timeString) {
    time:Time timeStruct = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = timeStruct.subtractDuration(1, 1, 1, 1, 1, 1, 1);
    timeString = timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testToTimezone () (string timeStrBefore, string timeStrAfter) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    timeStrBefore = timeStruct.toString();
    timeStruct = timeStruct.toTimezone("Asia/Colombo");
    timeStrAfter = timeStruct.toString();
    return;
}

function testToTimezoneWithInvalidZone () (string timeString) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1456876583555, zone:zoneValue};
    timeStruct = timeStruct.toTimezone("test");
    timeString = timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testToTimezoneWithDateTime () (string timeString) {
    time:Time timeStruct = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = timeStruct.toTimezone("Asia/Colombo");
    timeString = timeStruct.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testManualTimeCreate () (string timeString) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = {time:1498488382000, zone:zoneValue};
    timeString = time.toString();
    return;
}

function testManualTimeCreateWithNoZone () (int year) {
    time:Time time = {time:1498488382555};
    year = time.year();
    return;
}

function testManualTimeCreateWithEmptyZone () (int year) {
    time:Timezone zoneValue = {zoneId:""};
    time:Time time = {time:1498488382555, zone: zoneValue};
    year = time.year();
    return;
}

function testManualTimeCreateWithInvalidZone () (int year) {
    time:Timezone zoneValue = {zoneId:"test"};
    time:Time time = {time:1498488382555, zone:zoneValue};
    year = time.year();
    return;
}

function testParseTimenvalidPattern () (int timeValue, string zoneId, int zoneoffset) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "test");
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testParseTimenFormatMismatch () (int timeValue, string zoneId, int zoneoffset) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd");
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testFormatTimeInvalidPattern () (string timeString) {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time timeStruct = {time:1498488382444, zone:zoneValue};
    timeString = timeStruct.format("test");
    return;
}

function testParseTimeWithDifferentFormats () (int year, int month, int day, int hour, int minute, int second, int milliSecond,
                                               string dateStr, string dateZoneStr, string timeZoneStr, string datetimeStr) {
    time:Time timeStruct = time:parse("2017", "yyyy");
    year = timeStruct.year();
    timeStruct = time:parse("03", "MM");
    month = timeStruct.month();
    timeStruct = time:parse("31", "dd");
    day = timeStruct.day();
    timeStruct = time:parse("16", "HH");
    hour = timeStruct.hour();
    timeStruct = time:parse("59", "mm");
    minute = timeStruct.minute();
    timeStruct = time:parse("58", "ss");
    second = timeStruct.second();
    timeStruct = time:parse("999", "SSS");
    milliSecond = timeStruct.milliSecond();
    timeStruct = time:parse("2017/09/23", "yyyy/MM/dd");
    dateStr = timeStruct.format("yyyy-MM-dd");
    timeStruct = time:parse("2015/02/15+0800", "yyyy/MM/ddZ");
    dateZoneStr = timeStruct.format("yyyy-MM-ddZ");
    timeStruct = time:parse("08/23/59.544+0700", "HH/mm/ss.SSSZ");
    timeZoneStr = timeStruct.format("HH-mm-ss-SSS:Z");
    timeStruct = time:parse("2014/05/29-23:44:59.544", "yyyy/MM/dd-HH:mm:ss.SSS");
    datetimeStr = timeStruct.format("yyyy-MM-dd-HH:mm:ss.SSS");
    return;
}
