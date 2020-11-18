string exFlow = " ";
function basicTupleTest () returns (string) {
    // Test 1;
    string z = ("test1 " + "expr");
    addValue(z);
    endTest();

    // Test 2
    var y = ("test2");
    addValue(y);
    endTest();

    // Test 3
    string c;
    int d;
    [c, d] = ["test3", 3];
    addValue(c);
    addValue(d.toString());
    endTest();

    // Test 4
    [string,int] f = ["test4", 4];
    var [g, h] = f;
    addValue(g);
    addValue(h.toString());
    endTest();

    // Test 5
    FooStruct foo5 = {x:"foo test5"};
    [string, FooStruct] i = ["test5",foo5];
    var [j, k] = i;
    addValue(j);
    addValue(k.x);
    endTest();
    return exFlow;
}

type FooStruct record {
    string x;
};

function addValue (string value) {
    exFlow += value;
    exFlow += " ";
}

function endTest(){
    addValue("\n");
}

function testFunctionInvocation() returns (string) {
    [string, float, string] i = ["y", 5.0, "z"];
    string x = testTuples("x", i);
    return x;
}

function testTuples (string x, [string, float, string] y) returns (string) {
    var [i, j, k] = y;
    return x + i + j.toString() + k;
}

function testFunctionReturnValue() returns (string) {
    [string, float, string] x = testReturnTuples("x");
    var [i, j, k] = x;
    return i + j.toString() + k;
}

function testReturnTuples (string a) returns ([string, float, string]) {
    [string, float, string] x = [a, 5.0, "z"];
    return x;
}

function testFunctionReturnValue2() returns [string, float] {
    var [i, j, k] = testReturnTuples("x");
    return [i + k, j];
}

function testIgnoredValue1 () returns string {
    [string, int] x = ["foo", 1];
    string a;
    [a, _] = x;
    return a;
}

function testIgnoredValue2 () returns string {
    [string, int, int] x = ["foo", 1, 2];
    string a;
    int c;
    [a, _, c] = x;
    return a;
}

function testIgnoredValue3 () returns string {
    [string, int, int] x = ["foo", 1, 2];
    string a;
    [a, _, _] = x;
    return a;
}

function testIgnoredValue4 () returns [string, boolean] {
    [string, [int, [int, boolean]]] x = ["foo", [1, [2, true]]];
    string a;
    boolean b;
    [a, [_, [_, b]]] = x;
    return [a, b];
}

function testIndexBasedAccess () returns [string, int, boolean] {
    [boolean, int, string] x = [true, 3, "abc"];
    boolean tempBool = x[0];
    int tempInt = x[1];
    string tempString = x[2];
    x[0] = false;
    x[1] = 4;
    x[2] = "def";
    return [x[2], x[1], x[0]];
}

type Person record {|
    string name;
|};

type Employee record {
    string name;
    boolean intern;
};

function testIndexBasedAccessOfRecords () returns [string, boolean, string, string, float] {
    Person p1 = { name: "Foo" };
    Person p2 = { name: "Bar" };
    Employee e1 = { name: "FooBar", intern: false };

    [Person, Employee, Person, float] x = [p1, e1, p2, 12.0];
    Person p3 = x[0];
    Employee e2 = x[1];
    Person p4 = x[2];

    Person p5 = { name: "NewFoo" };
    Person p6 = { name: "NewBar" };
    Employee e3 = { name: "NewFooBar", intern: true };
    x[0] = p5;
    x[1] = e3;
    x[2] = p6;
    x[3] = 15.5;
    return [x[0].name, x[1].intern, x[2].name, p3.name, x[3]];
}

function testDefaultValuesInTuples () returns [string, int, boolean, float] {
    [boolean, int, string, float] x = [false, 0, "", 0.0];
    return [x[2], x[1], x[0], x[3]];
}

function testTupleToArrayAssignment1() returns string[] {
    [string...] x = ["a", "b", "c"];
    string[] y = x;
    return y;
}

function testTupleToArrayAssignment2() returns string[] {
    [string, string, string] x = ["a", "b", "c"];
    string[3] y = x;
    return y;
}

function testArrayToTupleAssignment1() returns [string...] {
    string[] x = ["a", "b", "c"];
    [string...] y = x;
    return y;
}

function testArrayToTupleAssignment2() returns [string, string...] {
    string[3] x = ["a", "b", "c"];
    [string, string...] y = x;
    return y;
}

function testArrayToTupleAssignment3() returns [string, string[]] {
    string[3] x = ["a", "b", "c"];
    [string, string...] [i, ...j] = x;
    return [i, j];
}

function testArrayToTupleAssignment4() returns [string, string, string] {
    string[3] x = ["a", "b", "c"];
    [string, string, string] y = x;
    return y;
}

const x = "x";
const y = "y";

type someType [x, int];
type anotherType [y, string, NoFillerObject];
type oneMoreType [y, string];

type someOtherType someType | anotherType | oneMoreType;

function testTupleUnionExpectedType() {
    someOtherType st = [y, "a", new(1)];
    someOtherType st1 = ["y", "str"];

    assertEquality(true, st is anotherType);

    anotherType val = <anotherType> st;
    assertEquality(y, val[0]);
    assertEquality("a", val[1]);
    assertEquality(1, val[2].a);

    assertEquality(true, st1 is oneMoreType);

    oneMoreType val2 = <oneMoreType> st1;
    assertEquality(y, val2[0]);
    assertEquality("str", val2[1]);
}

class NoFillerObject {
    int a;

    function init(int arg) {
        self.a = arg;
    }
}

function testUnionRestDescriptor() {
    [int, string|boolean...] x = [1, "hi", true];

    assertEquality(3, x.length());
    assertEquality(1, x[0]);
    assertEquality("hi", x[1]);
    assertEquality(true, x[2]);

    x.push("after", false, "after2", true);

    assertEquality(7, x.length());
    assertEquality(1, x[0]);
    assertEquality("hi", x[1]);
    assertEquality(true, x[2]);
    assertEquality("after", x[3]);
    assertEquality(false, x[4]);
    assertEquality("after2", x[5]);
    assertEquality(true, x[6]);
}

function testAnonRecordsInTupleTypeDescriptor() {
    [string, record {| string name; |}[]...] tup = ["foo", [{name: "Pubudu"}]];

    assertEquality(2, tup.length());
    assertEquality("foo", tup[0]);
    assertEquality(<record {| string name; |}[]>[{name: "Pubudu"}], tup[1]);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}
