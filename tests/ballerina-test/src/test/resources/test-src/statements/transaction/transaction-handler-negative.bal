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
