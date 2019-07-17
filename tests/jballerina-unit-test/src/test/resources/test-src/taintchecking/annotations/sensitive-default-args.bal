public function main (string... args) {
    secureFunctionFirstDefaultParamSensitive("static", secureIn = "static", insecureIn2 = args[0]);
    secureFunctionSecondDefaultParamSensitive("static", insecureIn2 = args[0], secureIn = "static");
}

public function secureFunctionFirstDefaultParamSensitive (string insecureIn1,
                                                          @untainted string secureIn = "example",
                                                          string insecureIn2 = "example") {
}

public function secureFunctionSecondDefaultParamSensitive (string insecureIn1,
                                                           string insecureIn2 = "example",
                                                           @untainted string secureIn = "example") {
}
