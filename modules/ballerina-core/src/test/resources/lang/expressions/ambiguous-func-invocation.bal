function testAmbiguousFunctionInvocation() {
    int val = 9;
    ambiguousFunc(val);
}

function ambiguousFunc(double val) {
    double out = val * 2;
}

function ambiguousFunc(string val) {
    string result = val + "abc";
}



