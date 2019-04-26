
function testAnonFunc() returns string {
    function (string, string) returns (string) anonFunction =
                function (string x, string y) returns (string) {
                    return x + y;
                };
    return anonFunction.call("Hello ", "World.!!!");
}