function testTransaction(int i) (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
    } failed {
        a = a + " inFailed";
        if (i > 4) {
            retry 4;
        } else {
            retry 3;
        }
    } committed {
        a = a + " inTrx";
    }
    a = a + " end";
    return a;
}