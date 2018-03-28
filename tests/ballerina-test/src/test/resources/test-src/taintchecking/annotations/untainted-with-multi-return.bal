public function main (string[] args) {
    string returnValue3;
    string returnValue4;
    (returnValue3, returnValue4) = taintedReturnFirstReturnUntainted(args[0]);
    secureFunction(returnValue3, returnValue4);
    secureFunction(returnValue4, returnValue3);

    string returnValue7;
    string returnValue8;
    (returnValue7, returnValue8) = taintedReturnSecondReturnUntainted(args[0]);
    secureFunction(returnValue7, returnValue8);
    secureFunction(returnValue8, returnValue7);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function taintedReturnFirstReturnUntainted (string inputData) returns (@untainted string, string) {
    return (inputData, inputData);
}

public function taintedReturnSecondReturnUntainted (string inputData) returns (string, @untainted string) {
    return (inputData, inputData);
}
