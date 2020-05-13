public function main (string... args) {
    secureFunction(untaintedReturn(args[0]), untaintedReturn(args[0]));
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function untaintedReturn (string input) returns @untainted string {
    return input;
}
