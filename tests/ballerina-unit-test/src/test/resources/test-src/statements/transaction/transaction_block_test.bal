import ballerina/transactions;
import ballerina/io;

public type TrxError record {
    string message;
    error? cause;
    string data;
    !...
};

function testTransactionStmtWithCommitedAndAbortedBlocks(int failureThreshold, boolean abort_) returns (string) {
    string a = "";
    a = (a + "start");
    int count = 0;
    try {
        transaction with retries=2, onabort=onAbort, oncommit=onCommit {
            a = a + " inTrx";
            count = count + 1;
            int i = 0;
            if (count <= failureThreshold) {
                // simulated transaction abort
                error err = { message: "TransactionError" };
                throw err;
            }

            if (abort_) {
                abort;
            }

            a = a + " endTrx";
        } onretry {
            a = a + " retry";
        } committed {
            a = a + " committed";
        } aborted {
            a = a + " aborted";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = (a + " end");
    return a;
}

function testTransactionStmtWithCommitedAndAbortedBlocks_() returns (string) {
    string a = "";
    a = (a + "start");
    try {
        transaction with retries=2, onabort=onAbort, oncommit=onCommit {
            a = a + " inTrx";
            int i = 0;
            if (a == "start inTrx") {
                // simulated transaction abort
                error err = { message: "TransactionError" };
                throw err;
            }
            //abort;
            a = a + " endTrx";
        } onretry {
            a = a + " retry";
        } committed {
            a = a + " committed";
        } aborted {
            a = a + " aborted";
        }
    } catch (error err) {
        a = a + err.message;
    }
    a = (a + " end");
    return a;
}

function onAbort (string trxId) {
    io:println("onAbort");
    int i = 5;
}

function onCommit(string trxId) {
    io:println("onCommit");
    float f = 2.2;
}
