import ballerina/lang.'transaction as transactions;

function testInvalidTrxHandlers() returns string {
    string ss = "started";
    transactions:Info transInfo;
    var onRollbackFunc = function(boolean willTry) {
        ss = ss + " trxAborted";
    };

    var onCommitFunc = function(string info) {
        ss = ss + " trxCommited";
    };

    transaction {
        transInfo = transactions:info();
        transactions:onRollback(onRollbackFunc);
        transactions:onCommit(onCommitFunc);
        var commitRes = commit;
    }
    ss += " endTrx";
    return ss;
}
