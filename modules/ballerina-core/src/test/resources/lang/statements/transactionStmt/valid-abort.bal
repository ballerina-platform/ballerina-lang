function test ()(string) {
    string i = "st";
    transaction {
        transaction {
            i = i + " inTrx";
            abort;
        } aborted {
            i = i + " inAbt";
            abort;
        }
    } committed {
        i = i + " outCom";
    } aborted {
        i = i + " outAbt";
    }
    return i;
}