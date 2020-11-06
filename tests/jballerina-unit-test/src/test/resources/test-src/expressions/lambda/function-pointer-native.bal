function concatString(string a, string b) returns string {
    return a + b;
}

function test1() returns (string) {
    function (string, string) returns (string) concatFunction = concatString;
    string c = concatFunction("foo", "bar");
    return c;
}

