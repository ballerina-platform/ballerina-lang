function testTransactionRetry1 (int i) (string) {
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

function testTransactionAbort4 () {
    int i = 10;
    transaction {
        i = i + 1;
        abort;
        i = i + 2;
    } aborted {
    i = i + 10;
}
}

function testTransactionRetry (int i) (string) {
    string a = "start";
    transaction {
        a = a + " inTrx";
    } failed {
        retry 4;
        a = a + " inFailed";
    } committed {
    a = a + " inTrx";
}
a = a + " end";
return a;
       }

       function testTransactionAbort5 () {
int i = 10;
transaction {
i = i + 1;
if (i > 10) {
    abort;
    }
    while ( i < 40) {
i = i + 2;
if (i == 44) {
    break;
    int k = 9;
}
}
abort;
i = i + 2;
} aborted {
i = i + 10;
}
}
