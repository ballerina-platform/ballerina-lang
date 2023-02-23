function testElvisValueTypePositive() returns (int) {
    int|() x = 120;
    int b;
    b = x ?: 111;
    return b;
}

function testElvisValueTypeNegative() returns (int) {
    int|() x = ();
    int b;
    b = x ?: 111;
    return b;
}

function testElvisValueTypeNested() returns (int) {
    int|() x = ();
    int|() y = 3000;
    int b;
    b = x ?: (y ?: 1300);
    return b;
}

function testElvisRefTypePositive() returns (int|string) {
    int|string|() x = 2300;
    int|string b;
    b = x ?: 111;
    return b;
}

function testElvisRefTypeNegative() returns (int|string) {
    int|string|() x = ();
    int|string b;
    b = x ?: 111;
    return b;
}

function testElvisRefTypeNested() returns (int|string) {
    int|string|() x = ();
    int|string|() y = ();
    int|string b;
    int|string c = 4000;
    b = x ?: (y ?: c);
    return b;
}

function testElvisRefTypeNestedCaseTwo() returns (int|string) {
    int|string|() x = ();
    int|string|() y = "kevin";
    int|string b;
    int|string c = 4000;
    b = x ?: (y ?: c);
    return b;
}

public type Person record {
    string name;
    int age;
};

function testElvisRecordTypePositive() returns [string, int] {
    Person|() xP = { name: "Jim", age: 100 };
    Person dP = { name: "default", age: 0 };
    Person b = xP ?: dP;
    return [b.name, b.age];
}

function testElvisRecordTypeNegative() returns [string, int] {
    Person|() xP = ();
    Person dP = { name: "default", age: 0 };
    Person b = xP ?: dP;
    return [b.name, b.age];
}

public class Student {
    public string name;
    public int age;
    public string favSubject;

    function init(string n, int a, string favSub = "Maths") {
        self.name = n;
        self.age = a;
        self.favSubject = favSub;
    }
}

function testElvisObjectTypePositive() returns [string, int, string] {
    Student|() stu1 = new Student("Alice", 15);
    Student stu2 = new("Bob", 18, favSub = "Science");
    Student stu = stu1 ?: stu2;
    return [stu.name, stu.age, stu.favSubject];
}

function testElvisObjectTypeNegative() returns [string, int, string] {
    Student|() stu1 = ();
    Student stu2 = new("Bob", 18, favSub = "Science");
    Student stu = stu1 ?: stu2;
    return [stu.name, stu.age, stu.favSubject];
}

function testElvisTupleTypePositive() returns [string, int] {
    [string, int]|() xT = ["Jack", 23];
    [string, int] dT = ["default", 0];
    var rT = xT ?: dT;
    return rT;
}

function testElvisTupleTypeNegative() returns [string, int] {
    [string, int]|() xT = ();
    [string, int] dT = ["default", 0];
    var rT = xT ?: dT;
    return rT;
}

function testElvisNestedTupleTypeCaseOne() returns [string, int] {
    [string, int]|() xT = ["Jack", 23];
    [string, int]|() x2T = ["Jill", 77];
    [string, int] dT = ["default", 0];
    var rT = xT ?: (x2T ?: dT);
    return rT;
}

function testElvisNestedTupleTypeCaseTwo() returns [string, int] {
    [string, int]|() xT = ();
    [string, int]|() x2T = ["Jill", 77];
    [string, int] dT = ["default", 0];
    var rT = xT ?: (x2T ?: dT);
    return rT;
}

function testElvisNestedTupleTypeCaseThree() returns [string, int] {
    [string, int]|() xT = ();
    [string, int]|() x2T = ();
    [string, int] dT = ["default", 0];
    var rT = xT ?: (x2T ?: dT);
    return rT;
}

function testElvisAsArgumentPositive() {
    string[] filters = [];
    string? mergable1 = ();
    string s1 = mergable1 ?: "";
    filters.push(s1);
    filters.push(mergable1 ?: "");

    string? mergable2 = "str";
    string s2 = mergable2 ?: "";
    filters.push(s2);
    filters.push(mergable2 ?: "");

    byte[] a = [];
    byte? b = ();
    a.push(b ?: 255);
    a.push(b ?: 0);

    assertEquals(filters[0], "");
    assertEquals(filters[2], "str");
    assertEquals(filters[0], filters[1]);
    assertEquals(filters[2], filters[3]);
    assertEquals(a[0], 255);
    assertEquals(a[1], 0);
}

int? nilVal = ();
string? stringOrNilVal = "dummyStr";
int? intOrNilVal = 1;

configurable string CONFIGURABLE_1 = stringOrNilVal ?: "val2";
configurable int CONFIGURABLE_2 = intOrNilVal ?: -1;

configurable int|string CONFIGURABLE_3 = nilVal ?: stringOrNilVal ?: "val2";
configurable int|string CONFIGURABLE_4 = stringOrNilVal ?: -1;

function testElvisWithConfigurableVar() {
    assertEquals(CONFIGURABLE_1, "dummyStr");
    assertEquals(CONFIGURABLE_2, 1);
    assertEquals(CONFIGURABLE_3, "dummyStr");
    assertEquals(CONFIGURABLE_4, "dummyStr");
}

int|() x1 = ();
int|() y1 = 3000;
string a1 = (x1 ?: y1).toBalString();

int|() y2 = ();
string a2 = (x1 ?: y2).toBalString();

string y3 = "a";
string a3 = (x1 ?: y3).toString();

int|() x2 = 1;
string a4 = (x2 ?: y3).toBalString();

int:Signed8 y4 = 127;
string a5 = (x2 ?: y4).toString();
string a6 = (x1 ?: y4).toString();

byte y5 = 255;
string a7 = (x1 ?: y5).toString();

int|() x3 = -9223372036854775807;
string a8 = (x3 ?: y5).toString();

float y6 = 123.12;
string a9 = (x3 ?: y6).toString();
string a10 = (x1 ?: y6).toString();

decimal|() x4 = ();
float y7 = -12.312;
string a11 = (x4 ?: y7).toBalString();

decimal|() x5 = 1.12321;
string a12 = (x5 ?: y7).toString();

string:Char|() x6 = ();
string y8 = "b";
string a13 = (x6 ?: y8).toString();

string:Char|() x7 = "a";
string a14 = (x7 ?: y8).toString();

boolean? x8 = ();
int[]|() y9 = [1, 3];
string a15 = (x8 ?: y9).toBalString();

boolean? x9 = true;
string a16 = (x9 ?: y9).toBalString();

function testElvisWithLangValueMethodCallsModuleLevel() {
    assertEquals("3000", a1);
    assertEquals("()", a2);
    assertEquals("a", a3);
    assertEquals("1", a4);
    assertEquals("1", a5);
    assertEquals("127", a6);
    assertEquals("255", a7);
    assertEquals("-9223372036854775807", a8);
    assertEquals("-9223372036854775807", a9);
    assertEquals("123.12", a10);
    assertEquals("-12.312", a11);
    assertEquals("1.12321", a12);
    assertEquals("b", a13);
    assertEquals("a", a14);
    assertEquals("[1,3]", a15);
    assertEquals("true", a16);
}

function testElvisWithLangValueMethodCalls() {
    int|() x10 = ();
    int|() y10 = 3000;
    string b = (x10 ?: y10).toBalString();
    assertEquals("3000", b);

    y10 = ();
    b = (x10 ?: y10).toBalString();
    assertEquals("()", b);

    string y11 = "a";
    b = (x10 ?: y11).toString();
    assertEquals("a", b);

    x10 = 1;
    b = (x10 ?: y11).toBalString();
    assertEquals("1", b);

    int:Signed8 y12 = 127;
    b = (x10 ?: y12).toString();
    assertEquals("1", b);

    x10 = ();
    b = (x10 ?: y12).toString();
    assertEquals("127", b);

    byte y13 = 255;
    b = (x10 ?: y13).toString();
    assertEquals("255", b);

    x10 = -9223372036854775807;
    b = (x10 ?: y13).toString();
    assertEquals("-9223372036854775807", b);

    float y14 = 123.12;
    b = (x10 ?: y14).toString();
    assertEquals("-9223372036854775807", b);

    x10 = ();
    b = (x10 ?: y14).toString();
    assertEquals("123.12", b);

    decimal|() x11 = ();
    float y15 = -12.312;
    b = (x11 ?: y15).toBalString();
    assertEquals("-12.312", b);

    x11 = 1.12321;
    b = (x11 ?: y15).toString();
    assertEquals("1.12321", b);

    string:Char|() x12 = ();
    string y16 = "b";
    b = (x12 ?: y16).toString();
    assertEquals("b", b);

    x12 = "a";
    b = (x12 ?: y16).toString();
    assertEquals("a", b);

    boolean? x13 = ();
    int[]|() y17 = [1, 3];
    b = (x13 ?: y17).toBalString();
    assertEquals("[1,3]", b);

    x13 = true;
    b = (x13 ?: y17).toBalString();
    assertEquals("true", b);
}

string? x14 = ();
string? y18 = ();
string:Char? z1 = "v";
string? elvisOutput1 = x14 ?: y18 ?: z1;

string? x15 = "a";
string? elvisOutput2 = x15 ?: y18 ?: z1;

string? x16 = ();
string? y19 = "b";
string? elvisOutput3 = x16 ?: y19 ?: z1;

string? x17 = "a";
string? elvisOutput4 = x17 ?: y19 ?: z1;
string? elvisOutput5 = x17 ?: y19 ?: z1 ?: x14 ?: y19 ?: x15;
string? elvisOutput6 = x16 ?: y18 ?: x14 ?: y18 ?: z1 ?: x15;

int? x18 = ();
byte? y20 = 255;
() z2 = ();
int? elvisOutput7 = x18 ?: y20 ?: z2;

int? x19 = ();
int:Unsigned8? y21 = 255;
int:Signed8? z3 = -128;
int? elvisOutput8 = x19 ?: y21 ?: z3;

decimal? x20 = ();
float? y22 = 1.213123;
int:Signed8? z4 = -128;
float|decimal|int:Signed8? elvisOutput9 = x20 ?: y22 ?: z4;

function testNestedElvisWithoutParenthesisModuleLevel() {
    assertEquals("v", elvisOutput1);
    assertEquals("a", elvisOutput2);
    assertEquals("b", elvisOutput3);
    assertEquals("a", elvisOutput4);
    assertEquals("a", elvisOutput5);
    assertEquals("v", elvisOutput6);
    assertEquals(255, elvisOutput7);
    assertEquals(255, elvisOutput8);
    assertEquals(1.213123, elvisOutput9);
}

function testNestedElvisWithoutParenthesis() {
    string? x21 = ();
    string? y23 = ();
    string:Char? z5 = "v";
    string? elvisOutput10 = x21 ?: y23 ?: z5;

    string? x22 = "a";
    string? elvisOutput11 = x22 ?: y23 ?: z5;

    string? x23 = ();
    string? y24 = "b";
    string? elvisOutput12 = x23 ?: y24 ?: z5;

    string? x24 = "a";
    string? elvisOutput13 = x24 ?: y24 ?: z5;
    string? elvisOutput14 = x24 ?: y24 ?: z5 ?: x21 ?: y24 ?: x22;
    string? elvisOutput15 = x23 ?: y23 ?: x21 ?: y23 ?: z5 ?: x22;

    int? x25 = ();
    byte? y25 = 255;
    () z6 = ();
    int? elvisOutput16 = x25 ?: y25 ?: z6;

    int? x26 = ();
    int:Unsigned8? y26 = 255;
    int:Signed8? z7 = -128;
    int? elvisOutput17 = x26 ?: y26 ?: z7;

    decimal? x27 = ();
    float? y27 = 1.213123;
    int:Signed8? z8 = -128;
    float|decimal|int:Signed8? elvisOutput18 = x27 ?: y27 ?: z8;

    assertEquals("v", elvisOutput10);
    assertEquals("a", elvisOutput11);
    assertEquals("b", elvisOutput12);
    assertEquals("a", elvisOutput13);
    assertEquals("a", elvisOutput14);
    assertEquals("v", elvisOutput15);
    assertEquals(255, elvisOutput16);
    assertEquals(255, elvisOutput17);
    assertEquals(1.213123, elvisOutput18);
}

type OptionalInt int?;

function getInt(OptionalInt i) returns int {
    return i ?: 0;
}

function testElvisExprWithTypeRefType() {
    OptionalInt a = 1;
    int r1 = getInt(a);
    assertEquals(1, r1);

    OptionalInt b = ();
    int r2 = getInt(b);
    assertEquals(0, r2);
}

function getIntArray(int[]? & readonly i) returns int[] {
    return i ?: [1, 2, 3];
}

type OptionalReadOnlyIntArray int[]? & readonly;

function getIntArray2(OptionalReadOnlyIntArray i) returns int[] {
    return i ?: [30, 21];
}

function testElvisExprWithIntersectionTypes() {
    int[] a = getIntArray([1, 2]);
    assertEquals([1, 2], a);

    int[] b = getIntArray(());
    assertEquals([1, 2, 3], b);

    int[] c = getIntArray2([11]);
    assertEquals([11], c);

    int[] d = getIntArray2(());
    assertEquals([30, 21], d);
}

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquals(anydata expected, anydata actual) {
    if expected == actual {
        return;
    }

    typedesc<anydata> expT = typeof expected;
    typedesc<anydata> actT = typeof actual;
    string msg = "expected [" + expected.toString() + "] of type [" + expT.toString()
                            + "], but found [" + actual.toString() + "] of type [" + actT.toString() + "]";
    panic error(ASSERTION_ERROR_REASON, message = msg);
}
