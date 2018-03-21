xmlns "http://sample.com/wso2/a1" as ns0;
public function main (string[] args) {
    string x = "static";
    x += args[0];
    secureFunction(x, x);

    string x1 = args[0];
    x1 += "static";
    secureFunction(x1, x1);

    var x2, _ = <xml> "<root xmlns:ns3=\"http://sample.com/wso2/f\"></root>";
    x2@[ns0:foo1] = "bar1";
    x2@[ns0:foo1] += args[0];
    secureFunction(x2,x2);

    int x3;
    x3, _ = <int>args[0];
    x3++;
    secureFunction(x3,x3);
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
