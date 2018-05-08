function main (string... args) {
    secureFunction(returnString("static data").trim(), returnString("static data").trim());
}

public function returnString(string data) returns (string) {
    return data;
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
