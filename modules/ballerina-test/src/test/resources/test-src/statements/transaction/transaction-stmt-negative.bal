function testTransactionRetry1(int i) (string) {
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

function testTransactionRetry2(int i) (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
    } failed {
        a = a + " inFailed";
        retry 4.5;
    } committed {
        a = a + " inTrx";
    }
        a = a + " end";
    return a;
}

function testTransactionAbort1 () {
    int i = 10;
    transaction {
        i = i + 1;
    } aborted {
        i = i + 10;
        abort;
    }
}

function testTransactionAbort2 () {
    int i = 10;
    transaction {
        i = i + 1;
    } committed {
        i = i + 10;
        abort;
    }
}

function testTransactionAbort3 () {
    int i = 10;
    abort;
    i = i + 11;
}
