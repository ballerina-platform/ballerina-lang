xmlns "http://sample.com/wso2/a1" as ns0;
public function main(string... args) returns error? {
    string x = "static";
    x += "static";
    secureFunction(x, x);

    var x2 = int.convert("100");
    if (x2 is int) {
        x2 += 1;
        x2 -= 2 + 2;
        secureFunction(x2, x2);
    } else {
        panic x2;
    }
    return ();
}

public function secureFunction(@untainted any secureIn, any insecureIn) {

}
