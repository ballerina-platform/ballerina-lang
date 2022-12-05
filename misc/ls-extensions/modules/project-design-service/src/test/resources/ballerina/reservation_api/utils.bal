import ballerina/time;

function currentDate() returns string {
    time:Civil civil = time:utcToCivil(time:utcNow());
    return string `${civil.year}/${civil.month}/${civil.day}`;
}