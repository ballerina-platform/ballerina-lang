public function main (string[] args) {
    string returnValue3;
    string returnValue4;
    (returnValue3, returnValue4) = taintedReturnFirstReturnTainted();
    secureFunction(returnValue3, returnValue4);
    secureFunction(returnValue4, returnValue3);

    string returnValue7;
    string returnValue8;
    (returnValue7, returnValue8) = taintedReturnSecondReturnTainted();
    secureFunction(returnValue7, returnValue8);
    secureFunction(returnValue8, returnValue7);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function taintedReturnFirstReturnTainted () returns (@tainted string, string) {
    return ("staticValue", "staticValue");
}

public function taintedReturnSecondReturnTainted () returns (string, @tainted string) {
    return ("staticValue", "staticValue");
}
