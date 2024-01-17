import ballerina/lang.test;

public function testFieldAsFinalParameter() returns (int) {
    int i = 50;
    int x = bar(i);
    return x;
}

function bar(int a) returns (int) {
    int i = a;
    a = 500;
    return a;
}

function foo(int a) returns (int) {
    int i = a;
    a = 500;
    return a;
}

function baz(float f, string s, boolean b, json j) returns [float, string, boolean, json] {
    f = 5.3;
    s = "Hello";
    b = true;
    j = {"a":"b"};
    return [f, s, b, j];
}

function finalFunction() {
    int i = 0;
}

service /FooService on new test:MockListener(9090) {

}

function testCompound(int a) returns int {
    a += 10;
    return a;
}

listener test:MockListener ml = new (8080);

public function testChangingListenerVariableAfterDefining() {
    ml = new test:MockListener(8081);
}

function testRestParamFinal(string p1, string... p2) {
    p2 = ["a", "b"];
}

function (int a, int... b) testModuleLevelRestParamFinal = function (int i, int... b) {
        b = [];
    };

public function testLocalLevelRestParamFinal() {
    int[] arr = [];
    function (int a, int... b) func = function (int i, int... b) {
        b = arr;
    };
}

public function testLocalLevelRestParamFinalWithVar() {
    int[] arr = [];
    var func = function (int i, int... b) {
        b = arr;
    };
}
