public function main (string... args) {
    any anyExample = args[0];
    var stringValue = <string> anyExample;
    secureFunction(stringValue, stringValue);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
