function testTransactionHandlers() {
    int i = 0;
    try {
        transaction with retries = 4, oncommit = commitFunction, onabort = abortFunction {
            i = 2;
        } onretry {
            i = i + 1;
        }
    } catch (error err) {
        i = -1;
    }
    i = 2;
}

function commitFunction(int transactionid) {
    int k = 0;
}


function abortFunction(int transactionid) {
    int j = 0;
}

function testTransactionHandlers2() {
    int i = 0;
    try {
        transaction with retries = 4, oncommit = commitFunction2, onabort = abortFunction2 {
            i = 2;
        } onretry {
            i = i + 1;
        }
    } catch (error err) {
        i = -1;
    }
    i = 2;
}

function commitFunction2(string transactionid, string x) {
    int k = 0;
}

function abortFunction2(string transactionid, string x) {
    int j = 0;
}

function testTransactionHandlers3() {
    int i = 0;
    try {
        transaction with retries = 4, oncommit = commitFunction3, onabort = abortFunction3 {
            i = 2;
        } onretry {
            i = i + 1;
        }
    } catch (error err) {
        i = -1;
    }
    i = 2;
}

int a = 10;
string x = "hello";
function testTransactionHandlers4() {
    int i = 0;
    try {
        transaction with retries = 4, oncommit = a, onabort = x {
            i = 2;
        } onretry {
            i = i + 1;
        }
    } catch (error err) {
        i = -1;
    }
    i = 2;
}

function testTransactionHandlers5() {
    int i = 0;
    try {
        transaction with retries = 4, oncommit = commitFunction5, onabort = abortFunction5 {
            i = 2;
        } onretry {
            i = i + 1;
        }
    } catch (error err) {
        i = -1;
    }
    i = 2;
}

function commitFunction5(string transactionid) returns int {
    int k = 0;
    return k;
}

function abortFunction5(string transactionid) returns int {
    int j = 0;
    return j;
}

function testTransactionHandlers6() {
    int i = 0;
    try {
        transaction with retries = 4, oncommit = commitFunction6, onabort = abortFunction6 {
            i = 2;
        } onretry {
            i = i + 1;
        }
    } catch (error err) {
        i = -1;
    }
    i = 2;
}

function commitFunction6(string transactionid) {
    int k = 0;
    transaction {
        int i = 0;
    }
}

function abortFunction6(string transactionid) {
    int j = 0;
    transaction {
        int i = 0;
    }
}

function testTransactionHandlers7() {
    int i = 0;
    var func = commitFunction6;
    try {
        transaction with retries = 4, oncommit = func, onabort = abortFunction6 {
            i = 2;
        } onretry {
            i = i + 1;
        }
    } catch (error err) {
        i = -1;
    }
    i = 2;
}

function testTransactionHandlers8() {
    int i = 0;
    try {
        transaction with retries = 4 {
            i = 2;
        } onretry {
            i = i + 1;
            transaction {
                i = 3;
            }
        }
    } catch (error err) {
        i = -1;
    }
    i = 2;
}
