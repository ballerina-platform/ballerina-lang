function testElvisValueTypeNested () returns (int) {
    int|() x = ();
    int|() y = 3000;
    int b;
    b = x ?: y ?: 1300;
    return b;
}

function testElvisValueTypeNotMatchingTypeWithRHS () returns (int) {
    int|() x = 120;
    int b;
    b = x ?: "string";
    return b;
}

function testElvisValueTypeNotMatchingTypeWithLHS () returns (int) {
    string|() x = ();
    int b;
    b = x ?: 3;
    return b;
}

function testElvisAsArgumentNegative() {
    int[] a = [];
    int? b = 1;
    a.push(b ?: "");

    byte[] c = [];
    byte? d = ();
    c.push(d ?: 256);
}
