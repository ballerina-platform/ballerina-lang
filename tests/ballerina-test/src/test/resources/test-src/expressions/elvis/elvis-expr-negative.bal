function testElvisValueTypeNested () returns (int) {
    int|null x = null;
    int|null y = 3000;
    int b;
    b = x ?: y ?: 1300;
    return b;
}

function testElvisValueTypeNotMatchingTypeWithRHS () returns (int) {
    int|null x = 120;
    int b;
    b = x ?: "string";
    return b;
}

function testElvisValueTypeNotMatchingTypeWithLHS () returns (int) {
    string|null x = null;
    int b;
    b = x ?: 3;
    return b;
}