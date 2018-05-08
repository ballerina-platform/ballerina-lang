function main (string... args) {
    string staticValue = "test";
    staticValue = staticValue.trim();
    secureFunction(staticValue, staticValue);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
