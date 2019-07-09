public function main (string... args) {
    worker w1 {
        secureFunction("test2", "test2");
    }
    worker w2 {
        secureFunction("test2", "test2");
    }
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
