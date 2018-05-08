function main (string... args) {
    // untainted data replaced by tainted value, again replaced by untainted value.
    string data = "sample";
    data = data + args[0];
    data = "replace";
    secureFunction(data, data);

    // concat untainted values.
    string data1 = testConcatHelper("sample", "sample");
    secureFunction(data1, data1);
}

public function secureFunction (@sensitive string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

function testConcatHelper(string s1, string s2) returns (string) {
    return s1 + s2;
}
