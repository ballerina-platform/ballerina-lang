function testTransactionStmtVariableRetry() (string) {
    int retryCount =  getRetryCount();
    string a = "start";
    transaction with retries(retryCount){
        a = a + " inTrx";
    } failed {
        a = a + " inFailed";
    } committed {
        a = a + " inTrx";
    }
    a = a + " end";
    return a;
}

function getRetryCount () (int) {
    return 2;
}

