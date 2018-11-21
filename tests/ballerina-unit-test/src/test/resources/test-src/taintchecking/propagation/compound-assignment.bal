xmlns "http://sample.com/wso2/a1" as ns0;
public function main (string... args) returns error? {
    string x = "static";
    x += "static";
    secureFunction(x, x);

    int x2;
    x2 = check <int>"100";
    x2 += 1;
    secureFunction(x2,x2);
    return ();
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
