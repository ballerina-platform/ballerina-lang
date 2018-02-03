function testTransaction(int i) (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
    } failed {
        a = a + " inFailed";
        retry -4;
    } committed {
        a = a + " inTrx";
    }
    a = a + " end";
    return a;
}


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
