xmlns "http://sample.com/wso2/a1" as ns0;
function main (string... args) {
    string x = "static";
    x += args[0];
    secureFunction(x, x);

    string x1 = args[0];
    x1 += "static";
    secureFunction(x1, x1);

    int x3;
    x3 = check <int>args[0];
    x3++;
    secureFunction(x3,x3);
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
