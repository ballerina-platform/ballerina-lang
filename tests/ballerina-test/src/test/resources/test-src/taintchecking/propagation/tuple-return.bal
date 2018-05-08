function main (string... args) {
    string returnValue1;
    string returnValue2;

    (returnValue1, returnValue2) = testFunction("sample", "sample");
    secureFunction(returnValue1, returnValue1);
    secureFunction(returnValue2, returnValue2);
}

function testFunction (string x, string y) returns (string, string) {
    return ("staticValue", x + y);
}

public function secureFunction (@sensitive any secureIn, any insecureIn) {

}
