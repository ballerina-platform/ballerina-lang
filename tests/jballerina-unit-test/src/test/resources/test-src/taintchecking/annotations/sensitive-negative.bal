public function main (string... args) {
    secureFunctionFirstParamSensitive(args[0], "static");
    secureFunctionSecondParamSensitive("static", args[0]);
}

public function secureFunctionFirstParamSensitive (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function secureFunctionSecondParamSensitive (string insecureIn, @untainted string secureIn) {
    string data = secureIn + insecureIn;
}
