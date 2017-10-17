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

function testTransactionInvalidRetry(int i) (string) {
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

function testTransactionInvalidRetry1(int i) (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
    } failed {
        a = a + " inFailed";
        int index = 0;
        while(index <= 5) {
            index = index + 1;
            retry 5;
        }
    } committed {
        a = a + " inTrx";
    }
    a = a + " end";
    return a;
}


function testBreakWithinTransaction () (string) {
    int i = 0;
    while (i < 5) {
        i = i + 1;
        transaction {
            if (i == 2) {
                break;
            }
        }
    }
    return "done";
}

function testContinueWithinTransaction () (string) {
    int i = 0;
    while (i < 5) {
        i = i + 1;
        transaction {
            if (i == 2) {
                next;
            }
        }
    }
    return "done";
}
