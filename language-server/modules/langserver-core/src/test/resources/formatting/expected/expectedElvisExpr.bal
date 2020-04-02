function testElvisValueTypePositive() returns (int) {
    int|() x = 120;
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

function testElvisTupleTypePositive() returns [string, int] {
    [string, int]|() xT = ["Jack", 23];
    [string, int] dT = ["default", 0];
    var rT = xT ?: dT;
    return rT;
}

function testElvisTupleTypeNegative() returns [string, int] {
    [string, int]|() xT = ();
    [string, int] dT = ["default", 0];
    var rT =
        xT
        ?:
        dT
    ;
    return rT;
}
