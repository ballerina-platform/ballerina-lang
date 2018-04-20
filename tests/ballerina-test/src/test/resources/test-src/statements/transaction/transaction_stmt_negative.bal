function testTransactionAbort3() {
    int i = 10;
    abort;
    i = i + 11;
}

function testTransactionAbort4() {
    int i = 10;
    transaction {
        i = i + 1;
        abort;
        i = i + 2;
    }
}

function testTransactionAbort5() {
    int i = 10;
    transaction {
        i = i + 1;
        if (i > 10) {
            abort;
        }
        while (i < 40) {
            i = i + 2;
            if (i == 44) {
                break;
                int k = 9;
            }
        }
        abort;
        i = i + 2;
    }
}

function testBreakWithinTransaction() returns (string) {
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

function testNextWithinTransaction() returns (string) {
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

function testReturnWithinTransaction() returns (string) {
    int i = 0;
    while (i < 5) {
        i = i + 1;
        transaction {
            if (i == 2) {
                return "ff";
            }
        }
    }
    return "done";
}

function testInvalidDoneWithinTransaction() {
    string workerTest = "";

    int i = 0;
    transaction {
        workerTest = workerTest + " withinTx";
        if (i == 0) {
            workerTest = workerTest + " beforeDone";
            done;
        }
    }
    workerTest = workerTest + " beforeReturn";
    return;
}