import ballerina.lang.time;

function testCurrentTime () (int timeValue, string zoneId, int zoneoffset) {
    time:Time timeStruct = time:currentTime();
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testCreateTimeWithZoneID () (int timeValue, string zoneId, int zoneoffset) {
    time:Time timeStruct = time:createTime(1498488382000, "America/Panama");
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testCreateTimeWithOffset () (int timeValue, string zoneId, int zoneoffset) {
    time:Time timeStruct = time:createTime(1498488382000, "-05:00");
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testCreateTimeWithNoZone () (int timeValue, string zoneId, int zoneoffset) {
    time:Time timeStruct = time:createTime(1498488382000, "");
    timeValue = timeStruct.time;
    zoneId = timeStruct.zone.zoneId;
    zoneoffset = timeStruct.zone.zoneOffset;
    return;
}

function testCreateDateTime () (string timeString) {
    time:Time timeStruct = time:createDateTime(2017, 3, 28, 23, 42, 45, 554, "America/Panama");
    timeString = time:toString(timeStruct);
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
    time:Time timeStruct = time:createTime(1498488382000, "America/Panama");
    timeString = time:toString(timeStruct);
    return;
}

function testFormatTime () (string timeString) {
    time:Time timeStruct = time:createTime(1498488382444, "America/Panama");
    timeString = time:format(timeStruct, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testGetFunctions () (int year, int month, int day, int hour, int minute, int second, int milliSecond,
                             string weekday) {
    time:Time timeStruct = time:createTime(1456876583555, "America/Panama");
    year = time:year(timeStruct);
    month = time:month(timeStruct);
    day = time:day(timeStruct);
    hour = time:hour(timeStruct);
    minute = time:minute(timeStruct);
    second = time:second(timeStruct);
    milliSecond = time:milliSecond(timeStruct);
    weekday = time:weekday(timeStruct);
    return;
}

function testGetDateFunction () (int year, int month, int day) {
    time:Time timeStruct = time:createTime(1456876583555, "America/Panama");
    year, month, day = time:getDate(timeStruct);
    return;
}

function testGetTimeFunction () (int hour, int minute, int second, int milliSecond) {
    time:Time timeStruct = time:createTime(1456876583555, "America/Panama");
    hour, minute, second, milliSecond = time:getTime(timeStruct);
    return;
}

function testAddDuration () (string timeString) {
    time:Time timeStruct = time:parse("2017-06-26T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = time:addDuration(timeStruct, 1, 1, 1, 1, 1, 1, 1);
    timeString = time:format(timeStruct, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testSubtractDuration () (string timeString) {
    time:Time timeStruct = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = time:subtractDuration(timeStruct, 1, 1, 1, 1, 1, 1, 1);
    timeString = time:format(timeStruct, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testToTimezone () (string timeString) {
    time:Time timeStruct = time:createTime(1456876583555, "America/Panama");
    timeStruct = time:toTimezone(timeStruct, "Asia/Colombo");
    timeString = time:format(timeStruct, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testToTimezoneWithDateTime () (string timeString) {
    time:Time timeStruct = time:parse("2016-03-01T09:46:22.444-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    timeStruct = time:toTimezone(timeStruct, "Asia/Colombo");
    timeString = time:format(timeStruct, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return;
}

function testManualTimeCreate () (string timeString) {
    time:Timezone zoneValue = {zoneId:"America/Panama", zoneOffset:-18000};
    time:Time time = {time:1498488382000, zone:zoneValue};
    timeString = time:toString(time);
    return;
}



