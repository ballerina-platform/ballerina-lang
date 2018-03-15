public function main (string[] args) {
    secureFunction(untaintedNamedReturn(args[0]), untaintedNamedReturn(args[0]));
    secureFunction(untaintedReturn(args[0]), untaintedReturn(args[0]));
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function untaintedNamedReturn (string input) (@untainted{} string output) {
    output = input;
    return output;
}

public function untaintedReturn (string input) (@untainted{} string) {
    return input;
}
