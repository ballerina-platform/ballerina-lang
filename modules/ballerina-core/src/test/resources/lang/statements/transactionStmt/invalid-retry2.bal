function testTransaction(int i) (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
    } failed {
        retry 4;
        a = a + " inFailed";
    } committed {
        a = a + " inTrx";
    }
    a = a + " end";
    return a;
}