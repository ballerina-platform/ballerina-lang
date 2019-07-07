public function main (string... args) {
    string path = "";
    if (args.length() > 0) {
        path = "/home";
    } else {
        path = "/test";
    }
    secureFunction(path, path);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
