import ballerina/io;
import ballerina/lang.'int as ints;

function testPrint1() {
    io:println("Hello");
    io:println(sampleFunction1());
    io:println("Ballerina");
}

function testPrint2() {
    io:println("Hello");
    io:println(sampleFunction2());
    io:println("Ballerina");
}

function testPrint3() {
    io:println("Hello");
    io:println(sampleFunction3());
    io:println("Ballerina");
}

function testPrint4() {
    io:println("Hello");
    io:println(sampleFunction4());
    io:println("Ballerina");
}

function testPrintInMatchBlock() {
    match (sampleFunction1()){
        () => {
            io:println("Hello");
            io:println(sampleFunction2());
            io:println("Ballerina");
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
        io:println(hello);
        io:println(sampleFunction1());
        io:println("Ballerina");
    }
    wait w2;
}

function testNoReturnFuncInvocnInNilReturnFuncRetStmt() {
    io:println(nilReturnFuncInvokingNoRetFuncInRetStmt());
}

function testNilReturnFuncInvocnInNilReturnFuncRetStmt() {
    io:println(nilReturnFuncInvokingNilRetFuncInRetStmt());
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
    io:println("nil returns here");
    return noReturnFunc();
}

function nilReturnFuncInvokingNilRetFuncInRetStmt() returns () {
    io:println("nil returns here");
    return nilReturnFunc();
}

function noReturnFunc() {
    io:println("no returns here");
}

function nilReturnFunc() returns () {
    io:println("explicit nil returns here");
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

function testReturnsDuringValidCheck() returns error? {
    int x = <int>ints:fromString("15");
}

function testValidCheckWithExplicitReturn() returns error? {
    int x = <int>ints:fromString("15");
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

