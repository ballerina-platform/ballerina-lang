function testTransactionStmtVariableRetry() (string) {
    int retryCount =  getRetryCount();
    string a = "start";
    transaction {
        a = a + " inTrx";
    } failed {
        a = a + " inFailed";
        retry retryCount;
    } committed {
        a = a + " inTrx";
    }
    a = a + " end";
    return a;
}

function getRetryCount () (int) {
    return 2;
}

