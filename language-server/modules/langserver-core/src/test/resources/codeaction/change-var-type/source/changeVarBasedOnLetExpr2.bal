function foo() {
    int i = let string x = "s" in getString(s);
}

function getString(string s) returns string {
    return s;
}
