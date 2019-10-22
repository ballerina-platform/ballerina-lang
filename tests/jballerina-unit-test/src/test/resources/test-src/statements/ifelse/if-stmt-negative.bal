function testIfStmtWithIncompatibleType1(int x) returns boolean {
    if (x == 10) {
        return false;
    } else if (x == 20) {
        return true;
    }
}
