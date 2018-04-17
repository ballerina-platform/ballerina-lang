function main (string... args) {
    secureFunction(typeReturn(args[0]), typeReturn(args[0]));
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function typeReturn (string input) returns (string) {
    return input;
}
