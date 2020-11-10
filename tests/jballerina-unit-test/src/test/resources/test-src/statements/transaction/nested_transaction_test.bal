import ballerina/lang.'transaction as transactions;

function testRollback() {
    string|error x =  trap actualCode(0, false);
    if(x is string) {
        assertEquality("start fc-0 inTrx Commit endTrx end", x);
    }
}

function testCommit() {
    string|error x =  trap actualCode(0, false);
    if(x is string) {
        assertEquality("start fc-0 inTrx Commit endTrx end", x);
    }
}

function testPanic() {
    string|error x =  trap actualCode(1, false);
    if(x is string) {
        assertEquality("start fc-1 inTrx blowUp", x);
    }
}

function actualCode(int failureCutOff, boolean requestRollback) returns (string) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count = 0;
    transaction {
        transaction {
            a = a + " inTrx";
            count = count + 1;
            if (count <= failureCutOff) {
                a = a + " blowUp"; // transaction block panic scenario, Set failure cutoff to 0, for not blowing up.
                int bV = blowUp();
            }
            if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
                a = a + " Rollback";
                rollback;
            } else {
                a = a + " Commit";
                var i = commit;
            }
            a = a + " endTrx";
            a = (a + " end");
        }
        var ii = commit;
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

function testLocalTransaction1(int i) returns int|error {
    int x = i;

    transaction {
        transaction {
            x += 1;
            check commit;
        }

        transaction {
            x += 1;
            check commit;
        }
        check commit;
    }
    return x;
}

function testLocalTransaction2(int i) returns int|error {
    int x = i;

    transaction {
        transaction {
            x += 1;
            check commit;
        }
        check commit;
    }

    return x;
}

function testMultipleTrxBlocks() returns error? {
    int i = check testLocalTransaction1(1);
    int j = check testLocalTransaction2(i);

    assertEquality(4, j);
}

string ss = "";
function testTrxHandlers() returns string {
    ss = ss + "started";
    transactions:Info transInfo;
    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
        ss = ss + " trxAborted";
    };

    var onCommitFunc = function(transactions:Info? info) {
        ss = ss + " trxCommited";
    };

    transaction {
        transaction {
            transInfo = transactions:info();
            transactions:onRollback(onRollbackFunc);
            transactions:onCommit(onCommitFunc);
            trxfunction();
            var commitRes = commit;
        }
        var ii = commit;
    }
    ss += " endTrx";
    return ss;
}

transactional function trxfunction() {
    ss = ss + " within transactional func";
}

public function testTransactionInsideIfStmt() returns int {
    int a = 10;
    if (a == 10) {
        int c = 8;
        transaction {
            transaction {
                int b = a + c;
                a = b;
                var commitRes = commit;
            }
            var ii = commit;
        }
    }
    return a;
}

public function testArrowFunctionInsideTransaction() returns int {
    int a = 10;
    int b = 11;
    transaction {
        function (int, int) returns int arrow1 = (x, y) => x + y + a + b;
        a = arrow1(1, 1);
        transaction {
            int c = a + b;
            function (int, int) returns int arrow2 = (x, y) => x + y + a + b + c;
            a = arrow2(2, 2);
            var commitRes = commit;
        }
        var ii = commit;
    }
    return a;
}

public function testAssignmentToUninitializedVariableOfOuterScopeFromTrxBlock() returns int|string {
    int|string s;
    transaction {
        transaction {
            s = "init-in-transaction-block";
            var commitRes = commit;
        }
        var ii = commit;
    }
    return s;
}

function testTrxReturnVal() returns string {
    string str = "start";
    transaction {
        var ii = commit;
        transaction {
            str = str + " within transaction";
            var commitRes = commit;
            str = str + " end.";
            return str;
        }
    }
}

function testInvokingTrxFunc() returns string {
    string str = "start";
    string res = funcWithTrx(str);
    return res + " end.";

}

function funcWithTrx(string str) returns string {
    transaction {
        var ii = commit;
        transaction {
            string res = str + " within transaction";
            var commitRes = commit;
            return res;
        }
    }
}

function testTransactionLangLib() returns error? {
    string str = "";
    var rollbackFunc = function (transactions:Info info, error? cause, boolean willRetry) {
        if (cause is error) {
            str += " " + cause.message();
        }
    };

    transaction {
        transaction {
            readonly d = 123;
            transactions:setData(d);
            transactions:Info transInfo = transactions:info();
            transactions:Info? newTransInfo = transactions:getInfo(transInfo.xid);
            if(newTransInfo is transactions:Info) {
                assertEquality(transInfo.xid, newTransInfo.xid);
            } else {
                panic AssertionError(ASSERTION_ERROR_REASON, message = "unexpected output from getInfo");
            }
            transactions:onRollback(rollbackFunc);
            str += "In Trx";
            assertEquality(d, transactions:getData());
            check commit;
            str += " commit";
        }
        check commit;
    }
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

    panic AssertionError(ASSERTION_ERROR_REASON, message = "expected '" + expected.toString() + "', found '" + actual.toString () + "'");
}

function testWithinTrxMode() returns string {
    string ss = "";
    var onCommitFunc = function(transactions:Info? info) {
        ss = ss + " -> trxCommited";
    };

    transaction {
        transaction {
            ss = "trxStarted";
            string invoRes = testFuncInvocation();
            ss = ss + invoRes + " -> invoked function returned";
            transactions:onCommit(onCommitFunc);
            if (transactional) {
                ss = ss + " -> strand in transactional mode";
            }
            var commitRes = commit;
            if (!transactional) {
                ss = ss + " -> strand in non-transactional mode";
            }
            ss += " -> trxEnded.";
        }
        var ii = commit;
    }
    return ss;
}

function testFuncInvocation() returns string {
    string ss = " -> within invoked function";
    if (transactional) {
        ss = ss + " -> strand in transactional mode";
    }
    return ss;
}

function testUnreachableCode() returns string {
    string ss = "";
    var onCommitFunc = function(transactions:Info? info) {
        ss = ss + " -> trxCommited";
    };

    transaction {
        transaction {
            ss = "trxStarted";
            transactions:onCommit(onCommitFunc);
            var commitRes = commit;
            if (transactional) {
                //only reached when commit fails
                ss = ss + " -> strand in transactional mode";
            }
            ss += " -> trxEnded.";
        }
        var ii = commit;
    }
    return ss;
}

function testMultipleTrxReturnVal() returns string {
    string str = "start";
    int i = 0;
    transaction {
        i += 1;
        str += " -> within transaction 1";
        var commitRes1 = commit;
        if(i >= 3) {
            return str;
        }
        transaction {
            i += 1;
            str += " -> within transaction 2";
            var commitRes2 = commit;
            if(i >= 3) {
                return str;
            }
            transaction {
                i += 1;
                str += " -> within transaction 3";
                var commitRes3 = commit;
                str += " -> returned.";
                if(i >= 3) {
                    return str;
                }
            }
        }
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
    transaction {
        str += " -> within trx 1";
        var res1 = commit;
        transaction {
            var res2 = commit;
            str += " -> within trx 2";
            transaction {
                var res3 = commit;
                str += " -> within trx 3";
                return str;
            }
        }
    }
}

function testNestedMiddleReturn() returns string {
    string str = "start";
    transaction {
        str += " -> within trx 1";
        var res1 = commit;
        transaction {
            int count = 1;
            var res2 = commit;
            str += " -> within trx 2";
            if (count == 1) {
                return str;
            }
            transaction {
                var res3 = commit;
                str += " -> within trx 3 -> should not reach here";
                return str;
            }
        }
    }
}
