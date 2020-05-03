public function main (string... args) {
    string[] arr = ["static"];
    secureFunction("static", ...arr);
    secureFunction(args[0], ...arr);
}

public function secureFunction (string insecureIn, @untainted string... secureIn) {
}
