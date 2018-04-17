function main (string... args) {
    string path = "";
    if (lengthof args > 0) {
        path = "/home";
    } else {
        path = "/test";
    }
    secureFunction(path, path);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
