function main (string... args) {
    secureFunctionFirstDefaultParamSensitive("static", secureIn = args[0], insecureIn2 = "static");
    secureFunctionSecondDefaultParamSensitive("static", insecureIn2 = "static", secureIn = args[0]);
}

public function secureFunctionFirstDefaultParamSensitive (string insecureIn1,
                                                          @sensitive string secureIn = "example",
                                                          string insecureIn2 = "example") {
}

public function secureFunctionSecondDefaultParamSensitive (string insecureIn1,
                                                           string insecureIn2 = "example",
                                                           @sensitive string secureIn = "example") {
}
