function name1() {
    transaction {
        abort;
    }
}

function name2() {
    transaction with retries = 0 {
        int b = 0;
        if (true) {
            abort;
        }
    } onretry {
        int a = 0;
    }
}

function name3() {
    transaction with retries = 0, oncommit = commitFunction, onabort = abortFunction {
        int h = 0;
    } onretry {
        string s = "";
    }
}

function commitFunction(string transactionid) {

}

function abortFunction(string transactionid) {

}
