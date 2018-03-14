public function main (string[] args) {
    returnString();
}

public function returnString() {
    string data = taintedReturn();
    secureFunction(data, data);
}

public function taintedReturn()(@tainted{} string) {
    return "example";
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
