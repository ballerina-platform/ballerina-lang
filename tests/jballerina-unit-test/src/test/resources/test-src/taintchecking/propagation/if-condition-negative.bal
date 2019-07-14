public function main (string... args) {
    string path = "";
    if (args.length() > 0) {
        path = args[0];
    } else {
        path = "/home";
    }
    secureFunction(path, path);

    string path1 = "";
    if (args.length() > 0) {
        path1 = "/home";
    } else {
        path1 = args[0];
    }
    secureFunction(path1, path1);

    string path2 = args[0];
    if (args.length() > 0) {
        path2 = "/home";
    } else {
        // Empty
    }
    secureFunction(path2, path2);

    string path3 = args[0];
    if (args.length() > 0) {
        // Empty
    } else {
        path3 = "/home";
    }
    secureFunction(path3, path3);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
