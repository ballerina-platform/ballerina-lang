import ballerina.lang.time;

function testCurrentTime () (int timeValue) {
    time:Time timeStruct = time:currentTime();
    timeValue = timeStruct.time;
    string zoneId = timeStruct.zone.zoneId;
    int zoneoffset = timeStruct.zone.zoneOffset;
    return;
}