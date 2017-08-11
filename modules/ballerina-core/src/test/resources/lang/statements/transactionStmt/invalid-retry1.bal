function testTransactionr(int i) (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
    } committed {
        a = a + " inTrx";
        retry 4;
    }
    a = a + " end";
    return a;
}