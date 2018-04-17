function main (string... args) {
    string[] fruits = ["apple", "banana", "cherry"];

    foreach v in fruits {
        secureFunction(v, v);
    }
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
