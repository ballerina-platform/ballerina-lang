import ballerina/http;

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

listener http:MockListener ml = new http:MockListener(8080);

public function testChangingListenerVariableAfterDefining() {
    ml = new http:MockListener(8081);
}

service s on ml {}

public function testChangingServiceVariableAfterDefining() {
    s = service {};
}
