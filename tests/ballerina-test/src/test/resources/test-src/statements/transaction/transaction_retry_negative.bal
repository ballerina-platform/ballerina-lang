function testTransaction(int i) returns (string) {
    string a = "start";
    transaction with retries = -4 {
        a = a + " inTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}


function testTransactionStmtVariableRetry() returns (string) {
    int retryCount = getRetryCount();
    string a = "start";
    transaction with retries = retryCount {
        a = a + " inTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}

function getRetryCount() returns (int) {
    return 2;
}

function testTransactionRetry2(int i) returns (string) {
    string a = "start";
    transaction with retries = 4.5 {
        a = a + " inTrx";
    } onretry {
        a = a + " inFailed";
    }
    a = a + " end";
    return a;
}
