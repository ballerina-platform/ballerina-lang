function testAmbiguousFunctionInvocation() {
    string val = "sample";
    ambiguousFunc(val);
}

function ambiguousFunc(double val) {
    double out = val * 2;
}

function ambiguousFunc(int val) {
    string result = val + "abc";
}



