function testTransaction(int i) returns (string) {
    map<string> logMap = {"log":"start"};
    transaction with retries = -4 {
        logMap["log"] = <string>logMap["log"] + " inTrx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}

function testTransactionStmtVariableRetry() returns (string) {
    int retryCount = getRetryCount();
    map<string> logMap = {"log":"start"};
    transaction with retries = retryCount {
        logMap["log"] = <string>logMap["log"] + " inTrx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}

function getRetryCount() returns (int) {
    return 2;
}

function testTransactionRetry2(int i) returns (string) {
    map<string> logMap = {"log":"start"};
    transaction with retries = 4.5 {
        logMap["log"] = <string>logMap["log"] + " inTrx";
    } onretry {
        logMap["log"] = <string>logMap["log"] + " inFailed";
    }
    logMap["log"] = <string>logMap["log"] + " end";
    return <string>logMap["log"];
}
