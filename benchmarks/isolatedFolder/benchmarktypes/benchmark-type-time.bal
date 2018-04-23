
import ballerina/time;

public function benchmarkCurrentTimeFunction() {
    time:Time time = time:currentTime();
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
}

public function benchmarkCreateTimeWithZoneIDFunction() {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new(1498488382000, zoneValue);
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
}

public function benchmarkCreateTimeWithOffsetFunction() {
    time:Timezone zoneValue = {zoneId:"-05:00"};
    time:Time time = new(1498488382000, zoneValue);
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
}

public function benchmarkCreateTimeWithNoZoneFunction() {
    time:Timezone zoneValue = {zoneId:""};
    time:Time time = new(1498488382000, zoneValue);
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
}

public function benchmarkCreateDateTimeFunction() {
    time:Time time = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
}

public function benchmarkParseTimeFunction() {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    int timeValue = time.time;
    string zoneId = time.zone.zoneId;
    int zoneoffset = time.zone.zoneOffset;
}

public function benchmarkToStringWithCreateTimeFunction() {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new(1498488382000, zoneValue);
    string timeString = time.toString();
}

public function benchmarkFormatTimeFunction() {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new(1498488382444, zoneValue);
    string timeString = time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

public function benchmarkTimeGetFunctions() {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new(1456876583555, zoneValue);
    int year = time.year();
    int month = time.month();
    int day = time.day();
    int hour = time.hour();
    int minute = time.minute();
    int second = time.second();
    int milliSecond = time.milliSecond();
    string weekday = time.weekday();
}

public function benchmarkGetDateFunction() {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new(1456876583555, zoneValue);
    int year; int month; int day;
    (year, month, day) = time.getDate();
}

public function benchmarkGetTimeFunction() {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new(1456876583555, zoneValue);
    int hour; int minute; int second; int milliSecond;
    (hour, minute, second, milliSecond) = time.getTime();
}

public function benchmarkAddDurationFunction() {
    time:Time time = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time.addDuration(1, 1, 1, 1, 1, 1, 1);
    string timeString = time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

public function benchmarkSubtractDurationFunction() {
    time:Time time = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time.subtractDuration(1, 1, 1, 1, 1, 1, 1);
    string timeString = time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

public function benchmarkToTimezoneFunction() {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new(1456876583555, zoneValue);
    string timeStrBefore = time.toString();
    time = time.toTimezone("Asia/Colombo");
    string timeStrAfter = time.toString();
}

public function benchmarkToTimezoneFunctionWithDateTime() {
    time:Time time = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    time = time.toTimezone("Asia/Colombo");
    string timeString = time.format("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
}

public function benchmarkManualTimeCreateFunction() {
    time:Timezone zoneValue = {zoneId:"America/Panama"};
    time:Time time = new(1498488382000, zoneValue);
    string timeString = time.toString();
}

public function benchmarkManualTimeCreateFunctionWithNoZone() {
    time:Timezone zoneValue = {zoneId:""};
    time:Time time = new(1498488382555, zoneValue);
    int year = time.year();
}

public function benchmarkManualTimeCreateFunctionWithEmptyZone() {
    time:Timezone zoneValue = {zoneId:""};
    time:Time time = new(1498488382555, zoneValue);
    int yesr = time.year();
}

public function benchmarkParseTimeFunctionWithDifferentFormats() {
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
}

