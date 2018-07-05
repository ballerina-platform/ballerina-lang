import org.bar;
import ballerina/http;

@final public int globalFinalInt = 10;


public function testFinalGlobalVariable() returns (int, int, int) {
    int v1 = globalFinalInt;
    globalFinalInt = 20;
    int v2 = globalFinalInt;
    bar:globalBarInt = 40;
    string v3 = bar:globalBarString;
    bar:globalBarString = "fail";
    return (v1, v2, globalFinalInt);
}

public function testFieldAsFinalParameter() returns (int) {
    int i = 50;
    int x = bar(i);
    return x;
}

function bar(@final int a) returns (int) {
    int i = a;
    a = 500;
    return a;
}

function foo(int a) returns (int) {
    int i = a;
    a = 500;
    return a;
}

function baz(float f, string s, boolean b, json j) returns (float, string, boolean, json) {
    f = 5.3;
    s = "Hello";
    b = true;
    j = {"a":"b"};
    return (f, s, b, j);
}

@final function finalFunction() {
    int i = 0;
}

@final service<http:Service> FooService {

}
