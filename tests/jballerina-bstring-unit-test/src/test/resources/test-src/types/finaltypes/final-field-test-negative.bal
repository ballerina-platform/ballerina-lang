import ballerina/http;

final int globalFinalInt = 10;


public function testFinalGlobalVariable() {
    int v1 = globalFinalInt;
    globalFinalInt = 20;
}

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

service FooService on new http:Listener(9090) {

}

function testCompound(int a) returns int {
    a += 10;
    return a;
}


function testLocalFinalValueWithType() {
    final string name = "Ballerina";
    name = "ABC";
}

function testLocalFinalValueWithoutType() {
    final var name = "Ballerina";
    name = "ABC";
}

function testLocalFinalValueWithTypeInitializedFromFunction() {
    final string name = getName();
    name = "ABC";
}

function testLocalFinalValueWithoutTypeInitializedFromFunction() {
    final var name = getName();
    name = "ABC";
}

function getName() returns string {
    return "Ballerina";
}

listener http:MockListener ml = new http:MockListener(8080);

public function testChangingListenerVariableAfterDefining() {
    ml = new http:MockListener(8081);
}

service s on ml {}

public function testChangingServiceVariableAfterDefining() {
    s = service {};
}
