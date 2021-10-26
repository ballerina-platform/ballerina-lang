function testJsonPositive() returns (json) {
    json j1;
    json j2 = ();
    j1 = {"name":"Jack"};
    j1 = +j2;

    return j1;
}

function testJsonNegative() returns (json) {
    json j1;
    json j2 = ();
    j1 = {"name":"Jack"};
    j1 = -j2;

    return j1;
}

function testJsonNot() returns (json) {
    json j1;
    json j2 = ();
    j1 = {"name":"Jack"};
    j1 = !j2;

    return j1;
}

function testNotOperator() returns int {
    int i = !10;
    return i;
}

function testIncompatibleSubtypesWithUnaryOperators() {
    int:Unsigned8 x1 = -7;
    int:Signed8 x2 = +1000;

    byte x3 = 0;
    byte x4 = ~x3;

    int:Unsigned8 x5 = 0;
    int:Unsigned8 x6 = ~x5;
}

function testIncompatibleUnaryOperations() {
    decimal x1 = ~12.5d;
    float x2 = +~12.5;
    anydata x3 = ~12d;
    anydata x4 = !12d;
}
