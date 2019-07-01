
public function main (string... args) returns error? {
    string x = "static";
    x += "static";
    secureFunction(x, x);

    int x2 = check int.convert("100");
    x2 += 1;
    secureFunction(x2, x2);

    return ();
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
