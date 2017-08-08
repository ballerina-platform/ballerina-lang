function testTransaction(int i) (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
    } failed {
        a = a + " inFailed";
        retry 4.5;
    } committed {
        a = a + " inTrx";
    }
    a = a + " end";
    return a;
}