import ballerina/time;

function test1() returns (int){
    function (string, string|time:TimeFormat) returns (time:Time|error) parseFunction = time:parse;
    time:Time|error c = parseFunction("2017-07-20T00:00:00.000-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    if (c is time:Time) {
        return c.time;
    } else {
        return 0;
    }
}

