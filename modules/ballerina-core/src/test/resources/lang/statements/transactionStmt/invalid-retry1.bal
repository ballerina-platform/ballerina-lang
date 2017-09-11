function testTransaction(int i) (string) {
    string a = "start";
    transaction with retries(-4) {
        a = a + " inTrx";
    } failed {
        a = a + " inFailed";
    } committed {
        a = a + " inTrx";
    }
    a = a + " end";
    return a;
}