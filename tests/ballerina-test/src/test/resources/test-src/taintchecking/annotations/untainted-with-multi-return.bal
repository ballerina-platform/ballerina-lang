public function main (string[] args) {
    string returnValue1;
    string returnValue2;
    returnValue1, returnValue2 = taintedNamedReturnFirstReturnUntainted(args[0]);
    secureFunction(returnValue1, returnValue2);
    secureFunction(returnValue2, returnValue1);

    string returnValue3;
    string returnValue4;
    returnValue3, returnValue4 = taintedReturnFirstReturnUntainted(args[0]);
    secureFunction(returnValue3, returnValue4);
    secureFunction(returnValue4, returnValue3);

    string returnValue5;
    string returnValue6;
    returnValue5, returnValue6 = taintedNamedReturnSecondReturnUntainted(args[0]);
    secureFunction(returnValue5, returnValue6);
    secureFunction(returnValue6, returnValue5);

    string returnValue7;
    string returnValue8;
    returnValue7, returnValue8 = taintedReturnSecondReturnUntainted(args[0]);
    secureFunction(returnValue7, returnValue8);
    secureFunction(returnValue8, returnValue7);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function taintedNamedReturnFirstReturnUntainted (string inputData) (@untainted{} string output1, string output2) {
    output1 = inputData;
    output2 = inputData;
    return;
}

public function taintedReturnFirstReturnUntainted (string inputData) (@untainted{} string, string) {
    return inputData, inputData;
}

public function taintedNamedReturnSecondReturnUntainted (string inputData) (string output1, @untainted{} string output2) {
    output1 = inputData;
    output2 = inputData;
    return;
}

public function taintedReturnSecondReturnUntainted (string inputData) (string, @untainted{} string) {
    return inputData, inputData;
}
