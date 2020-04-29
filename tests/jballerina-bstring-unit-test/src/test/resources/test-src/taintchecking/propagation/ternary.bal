public function main (string... args) {
    string data = args.length() > 0 ? "example" : "example";
    secureFunction(data, data);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
