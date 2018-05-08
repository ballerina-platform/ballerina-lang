function main (string... args) {
    string path = "";
    if (lengthof args > 0) {
        path = args[0];
    } else {
        path = "/home";
    }
    secureFunction(path, path);

    string path1 = "";
    if (lengthof args > 0) {
        path1 = "/home";
    } else {
        path1 = args[0];
    }
    secureFunction(path1, path1);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
