
function testAnonFunc() returns string {
    function (string, string) returns (string) anonFunction =
                function (string x, string y) returns (string) {
                    return x + y;
                };
    return anonFunction.call("Hello ", "World.!!!");
}

function testFPPassing() returns int {
    function (int, string)  anonFunction =
                    function (int x, string y) {
                        string z = y + y;
                    };
    var fp = useFp(anonFunction);
    return fp.call(10, 20);
}

function useFp(function (int, string) fp) returns (function (int, int) returns (int)) {
    fp.call(10, "y");
    function (int, int) returns (int) fp2 =
                        function (int x, int y) returns (int) {
                            return x * y;
                        };
    return fp2;
}