public function main (string[] args) {
    secureFunction(taintedNamedReturn(), taintedNamedReturn());
    secureFunction(taintedReturn(), taintedReturn());
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function taintedNamedReturn () (@tainted{} string output) {
    output = "staticValue";
    return;
}

public function taintedReturn () (@tainted{} string) {
    return "staticValue";
}
