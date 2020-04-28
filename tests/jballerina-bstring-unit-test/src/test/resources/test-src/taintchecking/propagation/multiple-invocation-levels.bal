public function main (string... args) {
    normalFunction("Hello, World!");
}

public function normalFunction (string normalInput) {
    anotherNormalFunction(normalInput);
}

public function anotherNormalFunction (string anotherNormalInput) {
    secureFunction(anotherNormalInput, anotherNormalInput);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
