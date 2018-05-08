function main (string... args) {
    string name = args[0];
    string template = string `Hello {{name}}!!!`;
    secureFunction(template, template);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}
