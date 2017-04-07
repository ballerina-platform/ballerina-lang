function testAmbiguousFunctionInvocation() {
    string val = "sample";
    ambiguousFunc(val);
}

function ambiguousFunc(float val) {
    float out = val * 2;
}

function ambiguousFunc(int val) {
    string result = val + "abc";
}



