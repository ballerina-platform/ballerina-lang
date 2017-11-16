function test1()(int){
    function (string, string) returns (Time) parseFunction = parse;
    Time c = parseFunction("2017-07-20T00:00:00.000-0500", "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    return c.time;
}

