public function main (string... args) {
    secureFunction(returnString("static data").trim(), returnString("static data").trim());
    secureFunction(returnTaintedMarkedUntainted(), "");
}

public function returnString(string data) returns (string) {
    return data;
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

public function returnTainted() returns @tainted string {
    return "";
}

public function returnTaintedMarkedUntainted() returns @untainted string {
    return  returnTainted();
}
