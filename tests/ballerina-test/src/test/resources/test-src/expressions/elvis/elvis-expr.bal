function testElvisValueTypePositive () returns (int) {
    int|() x = 120;
    int b;
    b = x ?: 111;
    return b;
}

function testElvisValueTypeNegative () returns (int) {
    int|() x = ();
    int b;
    b = x ?: 111;
    return b;
}

function testElvisValueTypeNested () returns (int) {
    int|() x = ();
    int|() y = 3000;
    int b;
    b = x ?: (y ?: 1300);
    return b;
}

function testElvisRefTypePositive () returns (int|string) {
    int|string|() x = 2300;
    int|string b;
    b = x ?: 111;
    return b;
}

function testElvisRefTypeNegative () returns (int|string) {
    int|string|() x = ();
    int|string b;
    b = x ?: 111;
    return b;
}

function testElvisRefTypeNested () returns (int|string) {
    int|string|() x = ();
    int|string|() y = ();
    int|string b;
    int|string c = 4000;
    b = x ?: (y ?: c);
    return b;
}

function testElvisRefTypeNestedCaseTwo () returns (int|string) {
    int|string|() x = ();
    int|string|() y = "kevin";
    int|string b;
    int|string c = 4000;
    b = x ?: (y ?: c);
    return b;
}

public type Person {
    string name;
    int age;
};

function testElvisStructTypePositive () returns (string, int) {
    Person|() xP = {name:"Jim", age:100};
    Person dP = {name:"default", age:0};
    Person b = {};
    b = xP ?: dP;
    return (b.name, b.age);
}

function testElvisStructTypeNegative () returns (string, int) {
    Person|() xP = ();
    Person dP = {name:"default", age:0};
    Person b = {};
    b = xP ?: dP;
    return (b.name, b.age);
}

function testElvisTupleTypePositive () returns (string, int) {
    (string, int)|() xT = ("Jack", 23);
    (string, int) dT = ("default", 0);
    var rT = xT ?: dT;
    return rT;
}

function testElvisTupleTypeNegative () returns (string, int) {
    (string, int)|() xT = ();
    (string, int) dT = ("default", 0);
    var rT = xT ?: dT;
    return rT;
}

function testElvisNestedTupleTypeCaseOne () returns (string, int) {
    (string, int)|() xT = ("Jack", 23);
    (string, int)|() x2T = ("Jill", 77);
    (string, int) dT = ("default", 0);
    var rT = xT ?: (x2T ?: dT);
    return rT;
}

function testElvisNestedTupleTypeCaseTwo () returns (string, int) {
    (string, int)|() xT = ();
    (string, int)|() x2T = ("Jill", 77);
    (string, int) dT = ("default", 0);
    var rT = xT ?: (x2T ?: dT);
    return rT;
}

function testElvisNestedTupleTypeCaseThree () returns (string, int) {
    (string, int)|() xT = ();
    (string, int)|() x2T = ();
    (string, int) dT = ("default", 0);
    var rT = xT ?: (x2T ?: dT);
    return rT;
}