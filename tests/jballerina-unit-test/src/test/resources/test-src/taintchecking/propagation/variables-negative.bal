type Foo record {
    string var1;
    string var2;
};

public function main (string... args) {
    // untainted data - replaced by tainted value.
    string data = "sample";
    data = data + args[0];
    secureFunction(data, data);

    // concat untainted value with tainted value.
    string data1 = testConcatHelper("sample", args[0]);
    secureFunction(data1, data1);

    // tuple variable
    [string, string] t = [args[0], "sample"];
    [string, string] [a1, b1] = t;
    secureFunction(a1, b1);

    // tuple variable reference
    string a2;
    string b2;
    [a2, b2] = [args[0], "sample"];
    secureFunction(a2, b2);

    // record variable
    Foo f1 = {var1: args[0], var2: "sample"};
    Foo {var1: a3, var2: b3} = f1;
    secureFunction(a3, b3);

    // record variable reference
    string a4;
    string b4;
    Foo f2 = {var1: args[0], var2: "sample"};
    {var1: a4, var2: b4} = f2;
    secureFunction(a4, b4);
}

public function secureFunction (@untainted string secureIn, string insecureIn) {
    string data = secureIn + insecureIn;
}

function testConcatHelper(string s1, string s2) returns (string) {
    return s1 + s2;
}
