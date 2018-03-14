public function main (string[] args) {
    secureFunction(namedReturn(args[0]), namedReturn(args[0]));
    secureFunction(typeReturn(args[0]), typeReturn(args[0]));
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function namedReturn (string input) (string output) {
    output = input;
    return output;
}

public function typeReturn (string input) (string) {
    return input;
}
