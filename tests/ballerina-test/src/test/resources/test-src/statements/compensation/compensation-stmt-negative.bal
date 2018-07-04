function testScopeWithoutCompensation() {
    int a = 2;
    scope scopeB {
        a = 6;
    }
}

function testCompensateWithoutIdentifier() {
    int a = 2;
    scope scopeB {
        a = 3;
    } compensation(a) {
        a = 4;
    }
    a = a + 1;
    compensate;
}
