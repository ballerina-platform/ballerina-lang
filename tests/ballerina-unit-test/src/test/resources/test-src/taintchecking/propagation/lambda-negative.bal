public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function main (string... args) {
    function (string, string) returns (string) testLambda =
    function (string x, string y) returns (string) {
        string r = x + y;
        return r;
    };

    secureFunction(testLambda.call(args[0], args[0]), testLambda.call(args[0], args[0]));
}


