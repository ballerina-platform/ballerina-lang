public function main (string... args) {
    function (string, string) returns (string) testLambda =
    function (string x, string y) returns (string) {
        string r = x + y;
        return r;
    };

    secureFunction(testLambda("Hello ", "world.!!!"), testLambda("Hello ", "world.!!!"));
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
