import ballerina/lang.'int as ints;

xmlns "http://sample.com/wso2/a1" as ns0;
public function main (string... args) returns error? {
    string x = "static";
    x += args[0];
    secureFunction(x, x);

    string x1 = args[0];
    x1 += "static";
    secureFunction(x1, x1);

    var x3 = ints:fromString(args[0]);
    if (x3 is int) {
        x3 += 1;
        secureFunction(x3,x3);
    } else {
        panic x3;
    }
    return ();
}

public function secureFunction (@untainted any secureIn, any insecureIn) {

}
