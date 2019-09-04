import ballerina/time;

function testCurrentTime() returns [int, string, int] {
    time:Time time = time:currentTime();
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return [timeValue, zoneId, zoneoffset];
}

function testNanoTime() returns (int) {
    int nanoTime = time:nanoTime();
    return nanoTime;
}

function testCreateTimeWithZoneID() returns [int, string, int] {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return [timeValue, zoneId, zoneoffset];
}

function testCreateTimeWithOffset() returns [int, string, int] {
    time:TimeZone zoneValue = {id:"-05:00"};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return [timeValue, zoneId, zoneoffset];
}

function testCreateTimeWithNoZone() returns [int, string, int] {
    time:TimeZone zoneValue = {id:""};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    int timeValue = time.time;
    string zoneId = time.zone.id;
    int zoneoffset = time.zone.offset;
    return [timeValue, zoneId, zoneoffset];
}

function testCreateDateTime() returns (string) {
    string timeValue = "";
    var  retTime = time:createTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    if (retTime is time:Time) {
        timeValue = time:toString(retTime);
    }
    return timeValue;
}

function testCreateDateTimeWithInvalidZone() returns string|time:Error {
    var retTime = time:createTime(2017, 3, 28, 23, 42, 45, 554, "TEST");
    if (retTime is time:Error) {
        return retTime;
    } else {
        return time:toString(retTime);
    }
}

function testParseTime() returns [int, string, int] {
    var timeRet = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    int timeValue = 0;
    string zoneId = "";
    int zoneoffset = 0;
    if (timeRet is time:Time) {
        timeValue = timeRet.time;
        zoneId = timeRet.zone.id;
        zoneoffset = timeRet.zone.offset;
    }
    return [timeValue, zoneId, zoneoffset];
}

function testParseTimeWithTimePartOnly() returns [int, string] {
    string timeFormatted = "";
    int timeValue = 0;
    var timeRet = time:parse("09:46:22", "HH:mm:ss");
    if (timeRet is time:Time) {
        timeValue = timeRet.time;
        string|error formattedRet = time:format(timeRet, "HH:mm:ss");
        if (formattedRet is string) {
            timeFormatted = formattedRet;
        }
    }
    return [timeValue, timeFormatted];
}

function testParseRFC1123Time(string timestamp) returns [int, string, int] {
    var timeRet = time:parse(timestamp, time:TIME_FORMAT_RFC_1123);
    int timeValue = 0;
    string zoneId = "";
    int zoneoffset = 0;
    if (timeRet is time:Time) {
        timeValue = timeRet.time;
        zoneId = timeRet.zone.id;
        zoneoffset = timeRet.zone.offset;
    }
    return [timeValue, zoneId, zoneoffset];
}

function testToStringWithCreateTime() returns (string) {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382000, zone: zoneValue };
    return time:toString(time);
}

function testFormatTime() returns (string) {
    string retValue = "";
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382444, zone: zoneValue };
    var ret =  time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (ret is string ) {
        retValue = ret;
    }
    return retValue;
}

function testFormatTimeToRFC1123() returns (string) {
    string retValue = "";
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382444, zone: zoneValue };
    var ret = time:format(time, time:TIME_FORMAT_RFC_1123);
    if (ret is string ) {
        retValue = ret;
    }
    return retValue;
}

function testGetFunctions() returns [int, int, int, int, int, int, int, string] {
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
    return [year, month, day, hour, minute, second, milliSecond, weekday];
}

function testGetDateFunction() returns [int, int, int] {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    int year; int month; int day;
    [year, month, day] = time:getDate(time);
    return [year, month, day];
}

function testGetTimeFunction() returns [int, int, int, int] {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    int hour; int minute; int second; int milliSecond;
    [hour, minute, second, milliSecond] = time:getTime(time);
    return [hour, minute, second, milliSecond];
}

function testAddDuration() returns (string) {
    string formattedTime = "";
    var timeRet = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (timeRet is time:Time) {
        time:Time timeAdded = time:addDuration(timeRet, 1, 1, 1, 1, 1, 1, 1);
        var retStr = time:format(timeAdded, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        if (retStr is string) {
            formattedTime = retStr;
        }
    }
    return formattedTime;
}

function testSubtractDuration() returns (string) {
    string formattedTime = "";
    var timeRet = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (timeRet is time:Time) {
        time:Time timeSubs = time:subtractDuration(timeRet, 1, 1, 1, 1, 1, 1, 1);
        var retStr = time:format(timeSubs, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        if (retStr is string) {
            formattedTime = retStr;
        }
    }
    return formattedTime;
}

function testToTimezone() returns [string, string]|error {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    string timeStrBefore = time:toString(time);
    var retTime = time:toTimeZone(time, "Asia/Colombo");
    if (retTime is time:Time) {
        string timeStrAfter = time:toString(time);
        return [timeStrBefore, timeStrAfter];
    } else {
        return retTime;
    }

}

function testToTimezoneWithInvalidZone() returns string|error {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1456876583555, zone: zoneValue };
    var retTime = time:toTimeZone(time, "test");
    if (retTime is time:Time) {
        return time:format(time, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    } else {
        return retTime;
    }
}

function testToTimezoneWithDateTime() returns (string) {
    string formattedString = "";
    var timeRet = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (timeRet is time:Time) {
        var newTime = time:toTimeZone(timeRet, "Asia/Colombo");
        if (newTime is time:Time) {
            var retStr = time:format(newTime, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            if (retStr is string) {
                formattedString = retStr;
            }
        }
    }
    return formattedString;
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

function testParseTimeValidPattern() returns [int, string, int]|time:Error {
    var timeRet = time:parse("2017-06-26T09:46:22.444-0500", "test");
    int timeValue = 0;
    string zoneId = "";
    int zoneoffset = 0;
    if (timeRet is time:Error) {
        return timeRet;
    } else {
        timeValue = timeRet.time;
        zoneId = timeRet.zone.id;
        zoneoffset = timeRet.zone.offset;
        return [timeValue, zoneId, zoneoffset];
    }

}

function testParseTimeFormatMismatch() returns [int, string, int]|time:Error {
    var timeRet = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd");
    int timeValue = 0;
    string zoneId = "";
    int zoneoffset = 0;
    if (timeRet is time:Error) {
        return timeRet;
    } else {
        timeValue = timeRet.time;
        zoneId = timeRet.zone.id;
        zoneoffset = timeRet.zone.offset;
        return [timeValue, zoneId, zoneoffset];
    }
}

function testFormatTimeInvalidPattern() returns string|time:Error {
    time:TimeZone zoneValue = {id:"America/Panama"};
    time:Time time = { time: 1498488382444, zone: zoneValue };
    return time:format(time, "test");
}

function testParseTimeWithDifferentFormats() returns [int, int, int, int, int, int, int, string, string, string,
        string] {
    int year = 0;
    int month = 0;
    int day = 0;
    int hour = 0;
    int minute = 0;
    int second = 0;
    int milliSecond = 0;
    string dateStr = "";
    string dateZoneStr = "";
    string timeZoneStr = "";
    string datetimeStr = "";

    var timeRet = time:parse("2017", "yyyy");
    if (timeRet is time:Time) {
        year = time:getYear(timeRet);
    }
    timeRet = time:parse("03", "MM");
    if (timeRet is time:Time) {
        month = time:getMonth(timeRet);
    }
    timeRet = time:parse("31", "dd");
    if (timeRet is time:Time) {
        day = time:getDay(timeRet);
    }
    timeRet = time:parse("16", "HH");
    if (timeRet is time:Time) {
        hour = time:getHour(timeRet);
    }
    timeRet = time:parse("59", "mm");
    if (timeRet is time:Time) {
        minute = time:getMinute(timeRet);
    }
    timeRet = time:parse("58", "ss");
    if (timeRet is time:Time) {
        second = time:getSecond(timeRet);
    }
    timeRet = time:parse("999", "SSS");
    if (timeRet is time:Time) {
        milliSecond = time:getMilliSecond(timeRet);
    }
    timeRet = time:parse("2017/09/23", "yyyy/MM/dd");
    if (timeRet is time:Time) {
        var retStr = time:format(timeRet, "yyyy-MM-dd");
        if (retStr is string) {
            dateStr = retStr;
        }
    }
    timeRet = time:parse("2015/02/15+0800", "yyyy/MM/ddZ");
    if (timeRet is time:Time) {
        var retStr = time:format(timeRet, "yyyy-MM-ddZ");
        if (retStr is string) {
            dateZoneStr = retStr;
        }
    }
    timeRet = time:parse("08/23/59.544+0700", "HH/mm/ss.SSSZ");
    if (timeRet is time:Time) {
        var retStr = time:format(timeRet, "HH-mm-ss-SSS:Z");
        if (retStr is string) {
            timeZoneStr = retStr;
        }
    }
    timeRet = time:parse("2014/05/29-23:44:59.544", "yyyy/MM/dd-HH:mm:ss.SSS");
    if (timeRet is time:Time) {
        var retStr = time:format(timeRet, "yyyy-MM-dd-HH:mm:ss.SSS");
        if (retStr is string) {
            datetimeStr = retStr;
        }
    }
    return [year, month, day, hour, minute, second, milliSecond, dateStr, dateZoneStr, timeZoneStr, datetimeStr];
}
