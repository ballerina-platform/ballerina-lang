import ballerina/jballerina.java;
import ballerina/lang.'int as ints;

function testPrint1() {
    println("Hello");
    println(sampleFunction1());
    println("Ballerina");
}

function testPrint2() {
    println("Hello");
    println(sampleFunction2());
    println("Ballerina");
}

function testPrint3() {
    println("Hello");
    println(sampleFunction3());
    println("Ballerina");
}

function testPrint4() {
    println("Hello");
    println(sampleFunction4());
    println("Ballerina");
}

function testPrintInMatchBlock() {
    match (sampleFunction1()){
        () => {
            println("Hello");
            println(sampleFunction2());
            println("Ballerina");
        }
    }
}

function testPrintInWorkers() {
    worker w1 {
        string hello = "Hello";
        hello -> w2;
    }

    worker w2 {
        string hello = "";
        hello = <- w1;
        println(hello);
        println(sampleFunction1());
        println("Ballerina");
    }
    wait w2;
}

function testNoReturnFuncInvocnInNilReturnFuncRetStmt() {
    println(nilReturnFuncInvokingNoRetFuncInRetStmt());
}

function testNilReturnFuncInvocnInNilReturnFuncRetStmt() {
    println(nilReturnFuncInvokingNilRetFuncInRetStmt());
}

function sampleFunction1() {
    // Do nothing
}

function sampleFunction2() returns () {
    // Do nothing
}

function sampleFunction3() {
    return ();
}

function sampleFunction4() returns () {
    return ();
}

function nilReturnFuncInvokingNoRetFuncInRetStmt() returns () {
    println("nil returns here");
    return noReturnFunc();
}

function nilReturnFuncInvokingNilRetFuncInRetStmt() returns () {
    println("nil returns here");
    return nilReturnFunc();
}

function noReturnFunc() {
    println("no returns here");
}

function nilReturnFunc() returns () {
    println("explicit nil returns here");
    return;
}

function testMissingReturnInIfElse() returns int? {
    int x = 100;

    if (x > 200) {
        return 1;
    }
}

function testMissingReturnInNestedIfElse() returns string? {
    int x = 100;
    string foo = "Foo";

    if (x > 200) {
        return "asdf";
    } else {
        if (foo.length() < 5) {
            x = 5;
        } else {
            return foo;
        }
    }
}

function testReturningInMatch() returns string? {
    int x = 10;

    match x {
        2|4|6|8 => {return "even";}
        1|3|5|7|9 => {return "odd";}
    }
}

function testReturnsDuringValidCheck() returns error? { // doesn't have to explicitly return nil since the return type is a subtype of `error?`
    int x = checkpanic ints:fromString("15");
}

function testValidCheckWithExplicitReturn() returns error? {
    int x = checkpanic ints:fromString("15");
    return;
}

function testNilableInt() returns int? {
}

function testNilableFloat() returns float? {
}

function testNilableDecimal() returns decimal? {
}

function testNilableString() returns string? {
}

function testNilableBoolean() returns boolean? {
}

function testNilableByte() returns byte? {
}

function testNilableJSON() returns json? {
}

function testNilableJSON2() returns json {
}

function testNilableXML() returns xml? {
}

function testNilableMap() returns map<any>? {
}


// Arrays

function testNilableIntArray() returns int[]? {
}

function testNilableFloatArray() returns float[]? {
}

function testNilableDecimalArray() returns decimal[]? {
}

function testNilableStringArray() returns string[]? {
}

function testNilableBooleanArray() returns boolean[]? {
}

function testNilableByteArray() returns byte[]? {
}

function testNilableJSONArray() returns json[]? {
}

function testNilableXMLArray() returns xml[]? {
}


// Complex types

type OpenPerson record {
    string name;
    int age;
};

function testNilableOpenRecord() returns OpenPerson? {
}

function testNilableOpenRecordArray() returns OpenPerson[]? {
}

type ClosedPerson record {|
    string name;
    int age;
|};

function testNilableClosedRecord() returns ClosedPerson? {
}

function testNilableClosedRecordArray() returns ClosedPerson[]? {
}

class PersonObj {
    string name = "John Doe";
}

function testNilableObject() returns PersonObj? {
}

function testNilableObjectArray() returns PersonObj[]? {
}

function testNilableUnion() returns int|string|OpenPerson|() {
}

function testNilableUnionArray() returns (int|string|OpenPerson)?[]? {
}

function testNilableTuple() returns [int, string, OpenPerson]? {
}

function testNilableTupleArray() returns [int, string, OpenPerson][]? {
}

function testNilableTypedesc() returns typedesc<any>? {
}

function testNilableTypedescArray() returns typedesc<any>[]? {
}

type Object object {
    function foo() returns int?; // no warning
};

class MyClass {
    function foo() returns int? {
    }
}

function testFunctionNotExplicitlyReturingValue() returns int? {
    int? a = 10;
    if a is int {
        return 1;
    }
}

service object {} obj2 = service object {
    remote function foo() returns int? {
        int? a = 10;
        if a is int {
            return 1;
        }
    }
};

client class MyClient {
    remote function foo() returns int? {
    }
}

type MyClientObj client object {
    function foo() returns int?; // no warning
    remote function bar() returns int?; // no warning
};

function print(any|error... values) returns int? = @java:Method { // no warning
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;

public function println(any|error... values) = @java:Method {
    'class: "org.ballerinalang.test.utils.interop.Utils"
} external;
