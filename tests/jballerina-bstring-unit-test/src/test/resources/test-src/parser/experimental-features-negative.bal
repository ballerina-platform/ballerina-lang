function testTransactionStmtWithCommitedAndAbortedBlocks() returns string {
    string a = "";
    int count = 0;
    transaction with retries=2 {
        a = a + " inTrx";
        a = a + " endTrx";
    } onretry {
        a = a + " retry";
    } committed {
        a = a + " committed";
    } aborted {
        a = a + " aborted";
    }
    a = (a + " end");
    return a;
}

int counter = 10;

function testLock() {
    lock {
        counter = counter + 1;
    }
}
