public function main (string[] args) {
    function (string, string) returns (string) testLambda =
    function (string x, string y) returns (string) {
        string r = x + y;
        return r;
    };

    secureFunction(testLambda("Hello ", args[0]), testLambda("Hello ", args[0]));
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
