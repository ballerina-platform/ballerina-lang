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

//enable after #33655 is fixed
//configurable int|string CONFIGURABLE_3 = nilVal ?: stringOrNilVal ?: "val2";
configurable int|string CONFIGURABLE_4 = stringOrNilVal ?: -1;

function testElvisWithConfigurableVar() {
    assertEquals(CONFIGURABLE_1, "dummyStr");
    assertEquals(CONFIGURABLE_2, 1);
    //assertEquals(CONFIGURABLE_3, "dummyStr");
    assertEquals(CONFIGURABLE_4, "dummyStr");
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
