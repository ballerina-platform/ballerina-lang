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