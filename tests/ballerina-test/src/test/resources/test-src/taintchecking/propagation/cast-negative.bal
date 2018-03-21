public function main (string[] args) {
    any anyExample = args[0];
    string stringValue;
    stringValue, _ = (string) anyExample;
    secureFunction(stringValue, stringValue);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
