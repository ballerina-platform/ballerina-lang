function testCorrectTransactionBlock() returns (string) {
    map<string> logMap = {"log":""};
    transaction with retries=2 {
        } onretry {
            logMap["log"] = <string>logMap["log"] + " retry";
        } committed {
            logMap["log"] = <string>logMap["log"] + " committed";
        } aborted {
            logMap["log"] = <string>logMap["log"] + " aborted";
        }
    return <string>logMap["log"];
}

function testTwoAbortedBlocks() returns (string) {
    map<string> logMap = {"log":""};
    transaction with retries=2 {
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " committed";
    } aborted {
        logMap["log"] = <string>logMap["log"] + " aborted";
    }
    return <string>logMap["log"];
}

function testTwoCommittedBlocks() returns (string) {
    map<string> logMap = {"log":""};
    transaction with retries=2 {
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    } committed {
        logMap["log"] = <string>logMap["log"] + " aborted";
    }
    return <string>logMap["log"];
}

function testNestedTrxBlocks() returns (string) {
    map<string> logMap = {"log":""};
    transaction with retries=2 {
        transaction {
            a += "nested block";
        }
    } onretry {
        logMap["log"] = <string>logMap["log"] + " retry";
    } committed {
        logMap["log"] = <string>logMap["log"] + " committed";
    }
    return <string>logMap["log"];
}
