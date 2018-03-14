public function main (string[] args) {
    secureFunction(namedReturn("static"), namedReturn("static"));
    secureFunction(typeReturn("static"), typeReturn("static"));
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
