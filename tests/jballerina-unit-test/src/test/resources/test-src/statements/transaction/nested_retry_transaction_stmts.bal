import ballerina/lang.'transaction as transactions;

function testRetry() {
    string|error x = actualCode(2, false, false);
    if(x is string) {
        assertEquality("start fc-2 -> in trx1 -> trx1 failed -> in trx1 -> trx1 failed -> in trx1 -> trx1 commit "
        + "-> trx1 end. -> in trx2 -> trx2 failed -> in trx2 -> trx2 failed -> in trx2 -> trx2 commit -> trx2 end. " +
        "-> all trx ended.", x);
    }
}

function testPanic() {
    string|error x = actualCode(2, false, true);
}

function actualCode(int failureCutOff, boolean requestRollback, boolean doPanic) returns (string|error) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count1 = 0;
    retry transaction {
        a = a + " -> in trx1";
        count1 = count1 + 1;
        if (count1 <= failureCutOff) {
            a = a + " -> trx1 failed"; // transaction block panic error, Set failure cutoff to 0, for not blowing up.
            int bV = check trxError();
        }

        if (doPanic) {
            blowUp();
        }

        if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
            rollback;
            a = a + " -> trx1 rollback";
        } else {
            check commit;
            a = a + " -> trx1 commit";
        }
        a = a + " -> trx1 end.";

        int count2 = 0;

        retry transaction {
            a = a + " -> in trx2";
            count2 = count2 + 1;
            if (count2 <= failureCutOff) {
                a = a + " -> trx2 failed"; // transaction block panic error, Set failure cutoff to 0, for not blowing up.
                int bV = check trxError();
            }

            if (doPanic) {
                blowUp();
            }

            if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
                rollback;
                a = a + " -> trx2 rollback";
            } else {
                check commit;
                a = a + " -> trx2 commit";
            }
            a = a + " -> trx2 end.";
        }

    }
    a = (a + " -> all trx ended.");
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
    string a = "start";
    retry(2) transaction {
           a = a + " -> in trx1";
           check getError();
           a = a + " -> after Err";
           check commit;
           retry(2) transaction {
              a = a + " -> in trx2";
              check getError();
              a = a + " -> after Err";
              check commit;
           }
           a = a + " -> after trx1";
    }
    a = a + " -> after trx2";
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
        retry(2) transaction {
                var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
                    a = a + " -> trxRollbacked-1-1";
                 };

                var onCommitFunc = function(transactions:Info? info) {
                    a = a + " -> trxCommited-1-1";
                };
                a += " -> in-trx-1-1";
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
        var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
            a = a + " -> trxRollbacked-1-2";
         };

        var onCommitFunc = function(transactions:Info? info) {
            a = a + " -> trxCommited-1-2";
        };
        a += " -> in-trx-1-2";
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
    a += " -> end-1";

    retry(2) transaction {
            retry(2) transaction {
                var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
                    a = a + " -> trxRollbacked-2-1";
                };

                var onCommitFunc = function(transactions:Info? info) {
                    a = a + " -> trxCommited-2-1";
                };
                a += " -> in-trx-2-1";
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
        var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
            a = a + " -> trxRollbacked-2-2";
        };

        var onCommitFunc = function(transactions:Info? info) {
            a = a + " -> trxCommited-2-2";
        };
        a += " -> in-trx-2-2";
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
    a += " -> end-2";
    return a;
}

public class MyRetryManager {
   private int count;
   public function init(int count = 2) {
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
}

function testCustomRetryManager() returns string|error {
    string str = "start";
    int count1 = 0;
    retry<MyRetryManager> (3) transaction {
        str += " -> inside trx1 ";
        count1 = count1 + 1;
        if(count1 < 3) {
            str += (" -> attempt " + count1.toString() + ":error,");
            int bV = check trxError();
        } else {
            str += (" -> attempt "+ count1.toString());
            var commitRes = commit;
            str += " -> result commited -> trx1 end.";
        }
        int count2 = 0;
        retry<MyRetryManager> (3) transaction {
                str += " -> inside trx2 ";
                count2 = count2 + 1;
                if(count2 < 3) {
                    str += (" -> attempt " + count2.toString() + ":error,");
                    int bV = check trxError();
                } else {
                    str += (" -> attempt "+ count2.toString());
                    var commitRes = commit;
                    str += " -> result commited -> trx2 end.";
                }
        }
    }
    return str;
}

function testNestedTrxWithinRetryTrx() returns string|error {
    string str = "start";
    int count = 0;
    retry transaction {
        str += " -> inside trx1 ";
        count = count + 1;
        if(count < 3) {
            str += (" -> attempt " + count.toString() + ":error,");
            int bV = check trxError();
        } else {
            str += (" -> attempt "+ count.toString());
            var commitRes = commit;
            str += " -> result commited -> trx1 end.";
        }
        transaction {
            str += " -> inside trx2 ";
            var commitRes = commit;
            str += " -> result commited -> trx2 end.";
        }
    }
    return str;
}

function testNestedRetryTrxWithinTrx() returns string|error {
    string str = "start";
    transaction {
        int count = 0;
        str += " -> inside trx1 ";
        retry transaction {
                str += " -> inside trx2 ";
                count = count + 1;
                if(count < 3) {
                    str += (" -> attempt " + count.toString() + ":error,");
                    int bV = check trxError();
                } else {
                    str += (" -> attempt "+ count.toString());
                    var commitRes = commit;
                    str += " -> result commited -> trx2 end.";
                }
            }
        var commitRes = commit;
        str += " -> result commited -> trx1 end.";
    }
    return str;
}

function testNestedReturns () {
    string nestedInnerReturnRes = testNestedInnerReturn();
    assertEquality("start -> within trx 1 -> within trx 2 -> within trx 3", nestedInnerReturnRes);
    string nestedMiddleReturnRes = testNestedMiddleReturn();
    assertEquality("start -> within trx 1 -> within trx 2", nestedMiddleReturnRes);
}

function testNestedInnerReturn() returns string {
    string str = "start";
    retry transaction {
        str += " -> within trx 1";
        var res1 = commit;
        retry transaction {
            var res2 = commit;
            str += " -> within trx 2";
            retry transaction {
                var res3 = commit;
                str += " -> within trx 3";
                return str;
            }
        }
    }
}

function testNestedMiddleReturn() returns string {
    string str = "start";
    retry transaction {
        str += " -> within trx 1";
        var res1 = commit;
        retry transaction {
            int count = 1;
            var res2 = commit;
            str += " -> within trx 2";
            if (count == 1) {
                return str;
            }
            retry transaction {
                var res3 = commit;
                str += " -> within trx 3 -> should not reach here";
                return str;
            }
        }
    }
}
