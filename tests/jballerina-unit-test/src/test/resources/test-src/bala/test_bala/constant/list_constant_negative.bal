import testorg/foo;

function testIncompatibleAssignment() {
    [string, string, int...] _ = foo:l1;
    int[] _ = foo:l5;
}

function testInvalidUpdates() {
    var a = foo:l1;
    a[0] = "1";
    a.push("2");

    var b = foo:l7;
    b.push("l7");
}

function testInvalidAccess() {
    float[] _ = foo:l10;
    string[] _ = foo:l11;
}
