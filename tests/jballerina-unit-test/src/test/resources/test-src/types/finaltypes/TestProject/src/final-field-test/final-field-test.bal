import org.bar;

final var globalFinalInt = 10;
final string globalFinalString = "hello";


public function testFinalAccess() returns [int, int, int, int] {
    int v1 = globalFinalInt;
    int v2 = bar:getGlobalBarInt();
    return [v1, v2, globalFinalInt, bar:getGlobalBarInt()];
}

public function testFinalStringAccess() returns [string, string, string, string] {
    string v1 = globalFinalString;
    string v2 = bar:getGlobalBarString();
    return [v1, v2, globalFinalString, bar:getGlobalBarString()];
}

public function testFinalFieldAsParameter() returns (int) {
    int x = foo(globalFinalInt);
    return x;
}

public function testFieldAsFinalParameter() returns (int) {
    int i = 50;
    int x = bar(i);
    return x;
}


function foo(int a) returns (int) {
    int i = a;
    return i;
}

function bar(int a) returns (int) {
    int i = a;
    return a;
}


function testLocalFinalValueWithType() returns string {
    final string name = "Ballerina";
    return name;
}

function testLocalFinalValueWithoutType() returns string {
    final var name = "Ballerina";
    return name;
}

function testLocalFinalValueWithTypeInitializedFromFunction() returns string {
    final string name = getName();
    return name;
}

function testLocalFinalValueWithoutTypeInitializedFromFunction() returns string {
    final var name = getName();
    return name;
}

function getName() returns string {
    return "Ballerina";
}
