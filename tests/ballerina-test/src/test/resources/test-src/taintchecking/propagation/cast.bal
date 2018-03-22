public function main (string[] args) {
    any anyExample = "staticValue";
    var conversionOutput = <string> anyExample;
    match conversionOutput {
        string stringValue => {
            secureFunction(stringValue, stringValue);
        }
        error err => return;
    }
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

