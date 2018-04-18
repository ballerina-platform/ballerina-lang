function main (string... args) {
    function (string, string) returns (string) testLambda =
    function (@sensitive string x,@sensitive  string y) returns (@tainted string) {
        string r = x + y;
        return r;
    };

    secureFunctionFirstParamSensitive(testLambda("Hello ", "world.!!!"));
}

public function secureFunctionFirstParamSensitive (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
