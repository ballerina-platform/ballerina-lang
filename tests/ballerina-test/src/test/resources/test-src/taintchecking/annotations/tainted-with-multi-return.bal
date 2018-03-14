public function main (string[] args) {
    string returnValue1;
    string returnValue2;
    returnValue1, returnValue2 = taintedNamedReturnFirstReturnTainted();
    secureFunction(returnValue1, returnValue2);
    secureFunction(returnValue2, returnValue1);

    string returnValue3;
    string returnValue4;
    returnValue3, returnValue4 = taintedReturnFirstReturnTainted();
    secureFunction(returnValue3, returnValue4);
    secureFunction(returnValue4, returnValue3);

    string returnValue5;
    string returnValue6;
    returnValue5, returnValue6 = taintedNamedReturnSecondReturnTainted();
    secureFunction(returnValue5, returnValue6);
    secureFunction(returnValue6, returnValue5);

    string returnValue7;
    string returnValue8;
    returnValue7, returnValue8 = taintedReturnSecondReturnTainted();
    secureFunction(returnValue7, returnValue8);
    secureFunction(returnValue8, returnValue7);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function taintedNamedReturnFirstReturnTainted () (@tainted{} string output1, string output2) {
    output1 = "staticValue";
    output2 = "staticValue";
    return;
}

public function taintedReturnFirstReturnTainted () (@tainted{} string, string) {
    return "staticValue", "staticValue";
}

public function taintedNamedReturnSecondReturnTainted () (string output1, @tainted{} string output2) {
    output1 = "staticValue";
    output2 = "staticValue";
    return;
}

public function taintedReturnSecondReturnTainted () (string, @tainted{} string) {
    return "staticValue", "staticValue";
}
