function testCorrectTransactionBlock() returns (string) {
    string a = "";
    transaction with retries=2 {
        } onretry {
            a = a + " retry";
        } committed {
            a = a + " committed";
        } aborted {
            a = a + " aborted";
        }
    return a;
}

function testTwoAbortedBlocks() returns (string) {
    string a = "";
    transaction with retries=2 {
    } onretry {
        a = a + " retry";
    } aborted {
        a = a + " committed";
    } aborted {
        a = a + " aborted";
    }
    return a;
}

function testTwoCommittedBlocks() returns (string) {
    string a = "";
    transaction with retries=2 {
    } onretry {
        a = a + " retry";
    } committed {
        a = a + " committed";
    } committed {
        a = a + " aborted";
    }
    return a;
}

function testNestedTrxBlocks() returns (string) {
    string a = "";
    transaction with retries=2 {
        transaction {
            a += "nested block";
        }
    } onretry {
        a = a + " retry";
    } committed {
        a = a + " committed";
    }
    return a;
}
