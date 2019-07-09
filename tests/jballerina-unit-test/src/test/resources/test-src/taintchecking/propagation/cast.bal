public function main (string... args) {
    any anyExample = "staticValue";
    var stringValue = <string> anyExample;
    secureFunction(stringValue, stringValue);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

