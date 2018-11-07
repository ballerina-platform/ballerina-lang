import ballerina/transactions;
import ballerina/io;

public type TrxError record {
    string message;
    error? cause;
    string data;
    !...
};

function testTransactionStmtWithCommitedAndAbortedBlocks(int failureCutOff, boolean requestAbort) returns (string) {
    string a = "";
    a = (a + "start");
    int count = 0;
    try {
        transaction with retries=2 {
            a = a + " inTrx";
            count = count + 1;
            int i = 0;
            if (count <= failureCutOff) {
                int bV = blowUp();
                if (bV == 0) {
                    a = a + " blown";
                } else {
                    a = a + " notBlown";
                }
            }

            if (requestAbort) {
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
        a = a + " [" + err.message + "]";
    }
    a = (a + " end");
    return a;
}

function blowUp()  returns int {
    if (5 == 5) {
        error err = { message: "TransactionError" };
        throw err;
    }
    return 5;
}
