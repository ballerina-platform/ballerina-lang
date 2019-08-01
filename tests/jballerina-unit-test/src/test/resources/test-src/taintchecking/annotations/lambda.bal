public function main (string... args) {
    function (string, string) returns (string) testLambda =
    function (@untainted string x,@untainted  string y) returns (@tainted string) {
        string r = x + y;
        return r;
    };

    secureFunctionFirstParamSensitive(testLambda("Hello ", "world.!!!"));
}

public function secureFunctionFirstParamSensitive (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
