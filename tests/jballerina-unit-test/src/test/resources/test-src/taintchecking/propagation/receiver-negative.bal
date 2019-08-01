public function main (string... args) {
    string example = args[0].trim();
    secureFunction(example, example);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
