function main (string... args) {
    // untainted data - replaced by tainted value.
    string data = "sample";
    data = data + args[0];
    secureFunction(data, data);

    // concat untainted value with tainted value.
    string data1 = testConcatHelper("sample", args[0]);
    secureFunction(data1, data1);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

function testConcatHelper(string s1, string s2) returns (string) {
    return s1 + s2;
}
