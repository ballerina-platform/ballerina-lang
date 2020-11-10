import ballerina/lang.'transaction as transactions;
import ballerina/io;

function testRetry() {
    string|error x = actualCode(2, false, false);
    if(x is string) {
        assertEquality("start fc-2 inTrx failed inTrx failed inTrx Commit endTrx end", x);
    }
}

function testPanic() {
    string|error x = actualCode(2, false, true);
}

function actualCode(int failureCutOff, boolean requestRollback, boolean doPanic) returns (string|error) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count = 0;

    retry transaction {
        a = a + " inTrx";
        count = count + 1;
        if (count <= failureCutOff) {
            a = a + " failed"; // transaction block panic error, Set failure cutoff to 0, for not blowing up.
            int bV = check trxError();
        }

        if (doPanic) {
            blowUp();
        }

        if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
            rollback;
            a = a + " Rollback";
        } else {
            check commit;
            a = a + " Commit";
        }
        a = a + " endTrx";
        a = (a + " end");
    }
    return a;
}

function trxError()  returns int|error {
    if (5 == 5) {
        return error("TransactionError");
    }
    return 5;
}

function blowUp() {
    panic error("TransactionError");
}

function transactionFailedHelper() returns string|error {
    string a = "";
    retry(2) transaction {
                 a = a + " inTrx";
                 check getError();
                 a = a + " afterErr";
                 check commit;
             }
    a = a + " afterTx";
    return a;
}

function getError() returns error? {
    return error("Generic Error", message = "Failed");
}

public function testFailedTransactionOutput() returns boolean {
    boolean testPassed = true;
    string|error result = transactionFailedHelper();
    testPassed = (result is error) && ("Generic Error" == result.message());
    return testPassed;
}

type AssertionError error;

const ASSERTION_ERROR_REASON = "AssertionError";

function assertEquality(any|error expected, any|error actual) {
    if expected is anydata && actual is anydata && expected == actual {
        return;
    }

    if expected === actual {
        return;
    }

    panic AssertionError(ASSERTION_ERROR_REASON,
            message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

function multipleTrxSequence(boolean abort1, boolean abort2, boolean fail1, boolean fail2) returns string {
    string a = "start";
    int count = 0;
    boolean failed1 = false;
    boolean failed2 = false;

    transactions:Info transInfo;

    retry(2) transaction {
        var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
            a = a + " trxRollbacked-1";
         };

        var onCommitFunc = function(transactions:Info? info) {
            a = a + " trxCommited-1";
        };
        a += " in-trx-1";
        transactions:onRollback(onRollbackFunc);
        transactions:onCommit(onCommitFunc);
        if ((fail1 && !failed1) || abort1) {
            if(abort1) {
              rollback;
            }
            if(fail1 && !failed1) {
              failed1 = true;
              error err = error("TransactionError");
              panic err;
            }
        } else {
            var commitRes = commit;
        }
    }
    a += " end-1";

    retry(2) transaction {
        var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
            a = a + " trxRollbacked-2";
        };

        var onCommitFunc = function(transactions:Info? info) {
            a = a + " trxCommited-2";
        };
        a += " in-trx-2";
        transactions:onRollback(onRollbackFunc);
        transactions:onCommit(onCommitFunc);
        if ((fail2 && !failed2) || abort2) {
           if(abort2) {
             rollback;
           }
           if(fail2 && !failed2) {
             failed2 = true;
             error err = error("TransactionError");
             panic err;
           }
        } else {
            var commitRes = commit;
        }
    }
    a += " end-2";
    return a;
}

public class MyRetryManager {
   private int count;
   public function init(int count = 2) {
       io:println("Count: ", count);
       self.count = count;
   }
   public function shouldRetry(error? e) returns boolean {
     io:println("Count: ", self.count);
     if e is error && self.count >  0 {
        self.count -= 1;
        return true;
     } else {
        return false;
     }
   }
}

function testCustomRetryManager() returns string|error {
    string str = "start";
    int count = 0;
    retry<MyRetryManager> (3) transaction {
        count = count+1;
        if(count < 3) {
            str += (" attempt " + count.toString() + ":error,");
            int bV = check trxError();
        } else {
            str += (" attempt "+ count.toString() + ":result returned end.");
            var commitRes = commit;
            return str;
        }
    }
    return str;
}
