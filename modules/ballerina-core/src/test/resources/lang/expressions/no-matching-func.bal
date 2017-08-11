function testAmbiguousFunctionInvocation() {
    string val = "sample";
    ambiguousFuncTest(val);
}

function ambiguousFuncTest(int val) {
    string result = val + "abc";
}



