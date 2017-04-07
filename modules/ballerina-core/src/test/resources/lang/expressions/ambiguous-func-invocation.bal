function testAmbiguousFunctionInvocation() {
    int val = 9;
    ambiguousFunc(val);
}

function ambiguousFunc(float val) {
    float out = val * 2;
}

function ambiguousFunc(string val) {
    string result = val + "abc";
}



