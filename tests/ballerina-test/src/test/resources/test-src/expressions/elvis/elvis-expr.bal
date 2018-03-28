function testElvisValueTypePositive () returns (int) {
    int|null x = 120;
    int b;
    b = x ?: 111;
    return b;
}

function testElvisValueTypeNegative () returns (int) {
    int|null x = null;
    int b;
    b = x ?: 111;
    return b;
}

function testElvisValueTypeNested () returns (int) {
    int|null x = null;
    int|null y = 3000;
    int b;
    b = x ?: (y ?: 1300);
    return b;
}

function testElvisRefTypePositive () returns (int|string) {
    int|string|null x = 2300;
    int|string b;
    b = x ?: 111;
    return b;
}

function testElvisRefTypeNegative () returns (int|string) {
    int|string|null x = null;
    int|string b;
    b = x ?: 111;
    return b;
}

function testElvisRefTypeNested () returns (int|string) {
    int|string|null x = null;
    int|string|null y = null;
    int|string b;
    int|string c = 4000;
    b = x ?: (y ?: c);
    return b;
}

function testElvisRefTypeNestedCaseTwo () returns (int|string) {
    int|string|null x = null;
    int|string|null y = "kevin";
    int|string b;
    int|string c = 4000;
    b = x ?: (y ?: c);
    return b;
}

struct Person {
    string name;
    int age;
}

function testElvisStructTypePositive () returns (string, int) {
    Person|null xP = {name:"Jim", age:100};
    Person dP = {name:"default", age:0};
    Person b = {};
    b = xP ?: dP;
    return (b.name, b.age);
}

function testElvisStructTypeNegative () returns (string, int) {
    Person|null xP = null;
    Person dP = {name:"default", age:0};
    Person b = {};
    b = xP ?: dP;
    return (b.name, b.age);
}

function testElvisTupleTypePositive () returns (string, int) {
    (string, int)|null xT = ("Jack", 23);
    (string, int) dT = ("default", 0);
    var rT = xT ?: dT;
    return rT;
}

function testElvisTupleTypeNegative () returns (string, int) {
    (string, int)|null xT = null;
    (string, int) dT = ("default", 0);
    var rT = xT ?: dT;
    return rT;
}

function testElvisNestedTupleTypeCaseOne () returns (string, int) {
    (string, int)|null xT = ("Jack", 23);
    (string, int)|null x2T = ("Jill", 77);
    (string, int) dT = ("default", 0);
    var rT = xT ?: (x2T ?: dT);
    return rT;
}

function testElvisNestedTupleTypeCaseTwo () returns (string, int) {
    (string, int)|null xT = null;
    (string, int)|null x2T = ("Jill", 77);
    (string, int) dT = ("default", 0);
    var rT = xT ?: (x2T ?: dT);
    return rT;
}

function testElvisNestedTupleTypeCaseThree () returns (string, int) {
    (string, int)|null xT = null;
    (string, int)|null x2T = null;
    (string, int) dT = ("default", 0);
    var rT = xT ?: (x2T ?: dT);
    return rT;
}