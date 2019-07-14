public function main (string... args) {
    string[] fruits = ["apple", args[0], "cherry"];

    foreach var v in fruits {
        secureFunction(v, v);
    }
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
