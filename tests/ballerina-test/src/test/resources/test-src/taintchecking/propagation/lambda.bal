public function main (string[] args) {
    function (string, string) returns (string) testLambda =
    (string x, string y) => (string) {
        string r = x + y;
        return r;
    };

    secureFunction(testLambda("Hello ", "world.!!!"), testLambda("Hello ", "world.!!!"));
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
