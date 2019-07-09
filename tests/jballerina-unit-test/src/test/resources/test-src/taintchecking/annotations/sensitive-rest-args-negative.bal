public function main (string... args) {
    secureFunction("static", ...args);
    secureFunction(args[0], ...args);
}

public function secureFunction (string insecureIn, @untainted string... secureIn) {
}
