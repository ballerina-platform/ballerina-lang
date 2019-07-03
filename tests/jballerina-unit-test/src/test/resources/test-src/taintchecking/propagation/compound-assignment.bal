
public function main (string... args) returns error? {
    string x = "static";
    x += "static";
    secureFunction(x, x);

    var x2 = int.convert("100");
    if (x2 is int) {
        x2 += 1;
        secureFunction(x2,x2);
    } else {
        panic x2;
    }
    return ();
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
