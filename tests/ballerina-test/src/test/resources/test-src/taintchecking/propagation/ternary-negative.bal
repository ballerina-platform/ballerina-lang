function main (string... args) {
    string data = lengthof args > 0 ? args[0] : "example";
    secureFunction(data, data);

    string data1 = lengthof args > 0 ? "example" : args[0];
    secureFunction(data1, data1);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
