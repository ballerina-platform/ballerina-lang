function main (string... args) {
    string data = lengthof args > 0 ? "example" : "example";
    secureFunction(data, data);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
