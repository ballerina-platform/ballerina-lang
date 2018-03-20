public function main (string[] args) {
    secureFunction(returnString("static data").trim(), returnString("static data").trim());
}

public function returnString(string data) (string) {
    return data;
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
