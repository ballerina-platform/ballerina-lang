function testAmbiguousFunctionInvocation() {
    string val = "sample";
    ambiguousFuncTest(val);
}

function ambiguousFuncTest(int val) {
    string result = val + "abc";
}

function testUndefinedFunction() {
    foo();
}

private function testMyFunc() {

}