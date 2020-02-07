public function main (string... args) {
    string data = args.length() > 0 ? args[0] : "example";
    secureFunction(data, data);

    string data1 = args.length() > 0 ? "example" : args[0];
    secureFunction(data1, data1);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
