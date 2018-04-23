function main (string... args) {
    secureFunction(returnString(args[0]).trim(), returnString(args[0]).trim());
}

public function returnString(string data) returns (string) {
    return data;
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
