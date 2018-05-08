function main (string... args) {
    secureFunctionFirstParamSensitive(args[0], "static");
    secureFunctionSecondParamSensitive("static", args[0]);
}

public function secureFunctionFirstParamSensitive (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function secureFunctionSecondParamSensitive (string insecureIn, @sensitive string secureIn) {
    string data = secureIn + insecureIn;
}
