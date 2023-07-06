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

function testTupleWithUnion() {
    [int]|[int, int, int] [a, b, ...c] = getTuples();
    assertEquality(1, a);
    assertEquality(2, b);
    assertEquality(3, c[0]);
}

function getTuples() returns [int]|[int, int, int] {
    return [1, 2, 3];
}

function testTupleDeclaredWithVar1() {
    var [a1, ...b1] = getData();
    assertEquality(1, a1);
    assertEquality(2, b1[0]);
    assertEquality("hello", b1[1]);
    assertEquality("world", b1[2]);

    var [a2, b2, ...c2] = getData();
    assertEquality(1, a2);
    assertEquality(2, b2);
    assertEquality("hello", c2[0]);
    assertEquality("world", c2[1]);

    var [...a3] = getData();
    assertEquality(1, a3[0]);
    assertEquality(2, a3[1]);
    assertEquality("hello", a3[2]);
    assertEquality("world", a3[3]);
}

function getData() returns [int, int, string...] {
    return [1, 2, "hello", "world"];
}

function testTupleDeclaredWithVar2() {
    var [a1, ...b1] = getInts();
    assertEquality(1, a1);
    assertEquality(2, b1[0]);
    assertEquality(3, b1[1]);
    assertEquality(4, b1[2]);

    var [a2, b2, ...c2] = getInts();
    assertEquality(1, a2);
    assertEquality(2, b2);
    assertEquality(3, c2[0]);
    assertEquality(4, c2[1]);

    var [...a3] = getInts();
    assertEquality(1, a3[0]);
    assertEquality(2, a3[1]);
    assertEquality(3, a3[2]);
    assertEquality(4, a3[3]);

    var [a4, b4, c4, d4] = getInts();
    assertEquality(1, a4);
    assertEquality(2, b4);
}

function getInts() returns int[4] {
    return [1, 2, 3, 4];
}

function testTupleDeclaredWithVar3() {
    var [a1, b1, ...c1] = getData2();
    assertEquality("Mike", a1);
    assertEquality(123, b1);
    assertEquality(false, c1[0]);
    assertEquality(321, c1[1]);
    assertEquality(32.56, c1[2]);
    assertEquality(100.4, c1[3]);
    assertEquality(5.0, c1[4]);
    assertEquality("Anne", c1[5]);

    var [a2, ...b2] = getData3();
    assertEquality("Mike", a2);
    assertEquality(233, b2[0][0]);
    assertEquality(123.45, b2[0][1]);
    assertEquality("Anne", b2[0][2][0]);
    assertEquality(23.2, b2[0][2][1]);
    assertEquality(12, b2[0][2][2][0]);
    assertEquality(false, b2[0][2][2][1]);
    assertEquality("John", b2[1][0]);
    assertEquality(true, b2[1][1] is int[]);
    int[] arr = <int[]> b2[1][1];
    assertEquality(1, arr[0]);
    assertEquality(23, arr[1]);
    assertEquality(421, arr[2]);
}

function getData2() returns [string , byte, boolean, int, (float|string)...] =>
                                    ["Mike", 123, false, 321, 32.56, 100.4, 5, "Anne"];

function getData3() returns [string, [int, float, [string, float|int, [int, boolean]]], [string, int[]]] =>
                                    ["Mike", [233, 123.45, ["Anne", 23.2, [12, false]]], ["John", [1,23,421]]];

function testTupleDeclaredWithVar4() {
    var [[intVar], {a: intVar2}, ...restBp] = getComplexTuple1();
    assertEquality(5, intVar);
    assertEquality(6, intVar2);
    assertEquality(true, restBp[0] is error);
    assertEquality("error msg", (<error>restBp[0]).message());
    int|error val1 = restBp[1];
    if (val1 is int) {
        assertEquality(12, val1);
    } else {
        panic getError("12", val1.toString());
    }
    int|error val2 = restBp[2];
    if (val2 is int) {
        assertEquality(13, val2);
    } else {
        panic getError("13", val2.toString());
    }

    var [a1, [b1, [c1, d1]], ...e1] = getComplexTuple2();
    assertEquality("Test", a1);
    assertEquality("Test", b1.name);
    assertEquality(23, b1.age);
    assertEquality(true, c1.b);
    assertEquality(56, c1.i);
    assertEquality("Fooo", d1.s);
    assertEquality(3.7, d1.f);
    assertEquality(23, d1.b);
    assertEquality(34, e1[0][0].id);
    assertEquality(true, e1[0][0].flag);
    assertEquality(56, e1[0][1]);

    var [a2, ...b2] = getComplexTuple2();
    assertEquality("Test", a2);
    assertEquality(foo, b2[0][0]);
    assertEquality(true, b2[0][1] is [BarObj, FooObj]);
    [BarObj, FooObj] objects = <[BarObj, FooObj]> b2[0][1];
    assertEquality(barObj, objects[0]);
    assertEquality(fooObj, objects[1]);
    assertEquality(bar, b2[1][0]);
    assertEquality(56, b2[1][1]);
}

type Foo record {
    string name;
    int age;
};

type Bar record {
    int id;
    boolean flag;
};

class FooObj {
    public string s;
    public float f;
    public byte b;
    public function init(string s, float f, byte b) {
        self.s = s;
        self.f = f;
        self.b = b;
    }
}

class BarObj {
    public boolean b;
    public int i;
    public function init(boolean b, int i) {
        self.b = b;
        self.i = i;
    }
}

Foo foo = {name: "Test", age: 23};
Bar bar = {id: 34, flag: true};
FooObj fooObj = new ("Fooo", 3.7, 23);
BarObj barObj = new (true, 56);

function getComplexTuple1() returns [[int], record {int a;}, error, int...] => [[5], {a: 6}, error("error msg"), 12, 13];

function getComplexTuple2() returns [string, [Foo, [BarObj, FooObj]], [Bar, int]] =>
                                       [foo.name, [foo, [barObj, fooObj]], [bar, barObj.i]];

function testTupleAsTupleFirstMember() {
    [[int, int]...] x = [[1, 2], [3, 4], [7, 8]];
    assertEquality(1, x[0][0]);
    assertEquality(2, x[0][1]);
    assertEquality(3, x[1][0]);
    assertEquality(4, x[1][1]);
    assertEquality(7, x[2][0]);
    assertEquality(8, x[2][1]);
}

function getError(string expectedVal, string actualVal) returns error {
    return error("expected " + expectedVal + " found " + actualVal);
}

type MyTupleType int|[MyTupleType...];

function testTupleToJSONAssignment() {
     [json, json...] A = [];
     json jsonTest = A;
     assertEquality(true, A is json);

     MyTupleType C = [1, 2, 3];
     jsonTest = C;
     assertEquality(true, C is json);

     [int, int...] B = [];
     jsonTest = B;
     assertEquality(true, B is json);

     [string, int] D = ["text1", 1];
     jsonTest = D;
     assertEquality(true, D is json);

     [string, int, boolean...] E = ["text1", 1, true, false];
     jsonTest = E;
     assertEquality(true, E is json);
     assertEquality(false, (<json[]>jsonTest)[2] is boolean[]);

     [float[]] F = [[10.5]];
     jsonTest = F;
     assertEquality(true, F is json);

     [map<string>...] G = [];
     jsonTest = G;
     assertEquality(true, G is json);
     assertEquality(true, jsonTest is map<string>[]);

     [float, ["json"], map<json>, int, json[], string...] H = [10.5];
     jsonTest = H;
     assertEquality(true, H is json);
     assertEquality(true, (<json[]>(<json[]>jsonTest)[1])[0] is string);

     xml testXml = xml `<elem>text1</elem>`;
     [string, xml]|json J = ["Anne", testXml];
     assertEquality(false, J is json);
     [string, xml[]|int]|json K = ["text1", [testXml]];
     assertEquality(false, K is json);
}

public const annotation tup on type;
public const annotation member on field;

type T1 [int, @member int, string...];
type T2 [int, @member int, string];
type T3 [@member int, string];

@tup
type T4 [@member int, string];

function testTupleMemberAnnotations2() returns [T1, T2, T3, T4] {
    T1 x1 =  [1, 2, "hello", "world"];
    T2 x2 =  [1, 2, "a"];
    T3 x3 =  [1, "hello"];
    T4 x4 =  [1, "a"];
    return [x1, x2, x3, x4];
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    string expectedValAsString = expected is error ? expected.toString() : expected.toString();
    string actualValAsString = actual is error ? actual.toString() : actual.toString();
    panic error(ASSERTION_ERROR_REASON,
                message = "expected '" + expectedValAsString + "', found '" + actualValAsString + "'");
}
