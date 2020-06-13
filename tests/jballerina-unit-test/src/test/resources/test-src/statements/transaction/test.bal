import ballerina/lang.'transaction as transactions;

public type DefaultRetryManager object {
    private int count;
    public function init(int count = 3) {
        self.count = count;
    }
    public function shouldRetry(error? e) returns boolean {
        if e is error && self.count >  0 {
          self.count -= 1;
          return true;
        } else {
           return false;
        }
    }
};

function testRollback() returns string|error {
    string|error x =  trap desugaredCode(0, true);
    return x;
}

function testCommit() returns string|error {
    string|error x =  trap desugaredCode(0, false);
    return x;
}

function testPanic() returns string|error {
    string|error x =  trap desugaredCode(1, false);
    return x;
}

//function actualCode(int failureCutOff, boolean requestRollback) returns (string) {
//    string a = "";
//    a = a + "start";
//    a = a + " fc-" + failureCutOff.toString();
//    int count = 0;
//
//    retry transaction {
//        a = a + " inTrx";
//        count = count + 1;
//        if (count <= failureCutOff) {
//            a = a + " blowUp"; // transaction block panic scenario, Set failure cutoff to 0, for not blowing up.
//            int bV = blowUp();
//        }
//        if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
//            a = a + " Rollback";
//            rollback;
//        } else {
//            a = a + " Commit";
//            var i = commit;
//        }
//        a = a + " endTrx";
//        a = (a + " end");
//    }
//
//    return a;
//}

function desugaredCode(int failureCutOff, boolean requestRollBack, int retryCount = 1) returns (string) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count = 0;

    string transactionBlockId = "";
    string transactionId = "";
    transactions:Info? prevAttempt = ();
    var trxFunc = function (transactions:Info? prev) returns error? {
        transactionId = transactions:startTransaction(transactionBlockId, prev);
        prevAttempt = transactions:info();
        a = a + " inTrx";
        count = count + 1;
        if (count <= failureCutOff) {
            a = a + " blowUp";
            int bV = blowUp();
        }

        if (requestRollBack) {
            error? rollbackResult = trap transactions:rollbackTransaction(transactionBlockId);
            if (rollbackResult is error) {

            } else {
                transactions:cleanupTransactionContext(transactionBlockId);
            }
            a = a + " Rollback";
        } else {
            boolean isFailed = transactions:getAndClearFailure();
            if (!isFailed) {
                var endSuccess = trap transactions:endTransaction(transactionId, transactionBlockId);
                if (endSuccess is string) {

                } else {
                    transactions:cleanupTransactionContext(transactionBlockId);
                }
            }
             a = a + " Commit";
        }
        a = a + " endTrx";
        a = (a + " end");
    };

    var result = trap trxFunc(prevAttempt);
    if (transactional && result is error) {
                error? rollbackResult = trap transactions:rollbackTransaction(transactionBlockId);
                if (rollbackResult is error) {

                } else {
                    transactions:cleanupTransactionContext(transactionBlockId);
                }
            }

    DefaultRetryManager dt = new;
    while (result is error && dt.shouldRetry(result)) {
        result = trap trxFunc(prevAttempt);
        if (transactional && result is error) {
            error? rollbackResult = trap transactions:rollbackTransaction(transactionBlockId);
            if (rollbackResult is error) {

            } else {
               transactions:cleanupTransactionContext(transactionBlockId);
            }
        }
    }
    return a;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = error("TransactionError");
        panic err;
    }
    return 5;
}
