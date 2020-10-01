import ballerina/io;
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
        a = a + " inTrx";
        count = count + 1;
        if transactional {
            io:println("Transactional mode");
        }
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

    io:println("## Transaction execution completed ##");
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
        x += 1;
        check commit;
    }

    transaction {
        x += 1;
        check commit;
    }
    return x;
}

function testLocalTransaction2(int i) returns int|error {
    int x = i;

    transaction {
        x += 1;
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
        transInfo = transactions:info();
        transactions:onRollback(onRollbackFunc);
        transactions:onCommit(onCommitFunc);
        trxfunction();
        var commitRes = commit;
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
            int b = a + c;
            a = b;
            var commitRes = commit;
        }
    }
    return a;
}

public function testArrowFunctionInsideTransaction() returns int {
    int a = 10;
    int b = 11;
    transaction {
        int c = a + b;
        function (int, int) returns int arrow = (x, y) => x + y + a + b + c;
        a = arrow(1, 1);
        var commitRes = commit;
    }
    return a;
}

public function testAssignmentToUninitializedVariableOfOuterScopeFromTrxBlock() returns int|string {
    int|string s;
    transaction {
        s = "init-in-transaction-block";
        var commitRes = commit;
    }
    return s;
}

function testTrxReturnVal() returns string {
    string str = "start";
    transaction {
        str = str + " within transaction";
        var commitRes = commit;
        str = str + " end.";
        return str;
    }
}

function testInvokingTrxFunc() returns string {
    string str = "start";
    string res = funcWithTrx(str);
    return res + " end.";

}

function funcWithTrx(string str) returns string {
    transaction {
        string res = str + " within transaction";
        var commitRes = commit;
        return res;
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
        ss = "trxStarted";
        transactions:onCommit(onCommitFunc);
        var commitRes = commit;
        if (transactional) {
            //only reached when commit fails
            ss = ss + " -> strand in transactional mode";
        }
        ss += " -> trxEnded.";
    }
    return ss;
}

function testTransactionalInvoWithinMultiLevelFunc() returns string {
    string ss = "";
    transaction {
        ss = "trxStarted";
        ss = func1(ss);
        var commitRes = commit;
        ss += " -> trxEnded.";
    }
    return ss;
}

transactional function func1(string str) returns string {
    string ss = func2(str);
    return ss + " -> within transactional func1";
}

transactional function func2(string str) returns string {
 transactions:Info transInfo = transactions:info();
 return str + " -> within transactional func2";
}

function testNewStrandWithTransactionalFunc() returns error? {
    string str = "";
    transaction {
        str += "trx started";
        var o = start testTransactionalInvo(ss);
        str += wait o;
        check commit;
        str += " -> trx end";
    }

    assertEquality("trx started -> transactional call -> trx end", str);
}

transactional function testTransactionalInvo(string str) returns string {
    return str + " -> transactional call";
}

function testRollbackWithBlockFailure() returns error? {
    string str = "";
    var onCommitFunc = function(transactions:Info? info) {
        str = str + " -> commit triggered";
    };

    var onRollbackFunc = function(transactions:Info? info, error? cause, boolean willTry) {
            str = str + " -> rollback triggered";
    };

    transaction {
        transactions:onCommit(onCommitFunc);
        transactions:onRollback(onRollbackFunc);
        str += "trx started";
        check getError(true);
        var o = commit;
        str += " -> trx end";
    }

    assertEquality("trx started -> rollback triggered", str);
}

function getError(boolean err) returns error? {
    if(err) {
       error er = error("Custom Error");
       return er;
    }
}

function testRollbackWithCommitFailure() returns error? {
    string str = "";
    var onCommitFunc = function(transactions:Info? info) {
        str = str + " -> commit triggered";
    };

    var rollbackFunc = function (transactions:Info info, error? cause, boolean willRetry) {
        str += "-> rollback triggered ";
    };

    transaction {
        transactions:onRollback(rollbackFunc);
        str += "trx started";
        setRollbackOnlyError();
        check commit;
        str += " commit";
    }
    str += "-> transaction block exited.";
    assertEquality("trx started-> transaction block exited.", str);
}

transactional function setRollbackOnlyError() {
    error cause = error("rollback only is set, hence commit failed !");
    transactions:setRollbackOnly(cause);
}

client class Bank {
    remote transactional function deposit(string str) returns string {
        return str + "-> deposit trx func";
    }

    function doTransaction() returns string {
        string str = "";
        transaction {
            str += "trx started ";
            var amount = self->deposit(str);
            str = amount;
            checkpanic commit;
        }
        return str;
    }
}

function testInvokeRemoteTransactionalMethodInTransactionalScope() {
    Bank bank = new;
    assertEquality("trx started -> deposit trx func", bank.doTransaction());
}

function testAsyncReturn() returns int {
    transaction {
        var x = start getInt();
        int f = wait x;
        var y = commit;
        return f;
    }
}

transactional function getInt() returns int {
    return 10;
}
