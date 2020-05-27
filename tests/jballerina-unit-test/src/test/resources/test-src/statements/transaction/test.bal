import ballerina/io;

function testRollback() returns string|error {
    //string|error x =  trap desugaredCode(0, true);

    string|error x =  trap actualCode(0, true);

    io:println(x);
    return x;
}

function testCommit() returns string|error {
    string|error x =  trap desugaredCode(0, false);
    io:println(x);
    return x;
}

function testPanic() returns string|error {
    string|error x =  trap desugaredCode(1, false);
    io:println(x);
    return x;
}


function actualCode(int failureCutOff, boolean requestRollback) returns (string) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count = 0;

    transaction {
        a = a + " inTrx";
        var i = commit;
        //count = count + 1;
        //if (count <= failureCutOff) {
        //    a = a + " blowUp"; // transaction block panic scenario, Set failure cutoff to 0, for not blowing up.
        //    int bV = blowUp();
        //}
        //if (requestRollback) { // Set requestRollback to true if you want to try rollback scenario, otherwise commit
        //    a = a + " Rollback";
        //    rollback;
        //} else {
        //    a = a + " Commit";
        //    var i = commit;
        //}
        //a = a + " endTrx";
        //a = (a + " end");
    }

    return a;
}

function desugaredCode(int failureCutOff, boolean requestRollBack) returns (string) {
    string a = "";
    a = a + "start";
    a = a + " fc-" + failureCutOff.toString();
    int count = 0;

    string transactionBlockId = "";
    string transactionId = transactions:startTransaction(transactionBlockId);

    var trxFunc = function () returns error? {
        var zz = function(transactions:Info? info, error? cause, boolean willTry) {
            io:println("######----RollbackedZZ-----#####");
        };
        transactions:onRollback(zz);

        var xx = function (transactions:Info? info) {
            io:println("#########----CommitedXX----###########");
        };
        var yy = function (transactions:Info? info) {
            io:println("#########----CommitedYY----###########");
        };
        transactions:onCommit(xx);
        transactions:onCommit(xx);
        transactions:onCommit(yy);

        a = a + " inTrx";
        count = count + 1;
        if (count <= failureCutOff) {
            a = a + " blowUp";
            int bV = blowUp();
            io:println("Transaction block Panic");
        }

        if (requestRollBack) {
            a = a + " Rollback";
            error? rollbackResult = trap transactions:rollbackTransaction(transactionBlockId);
            if (rollbackResult is error) {
                io:println("rollback failed ",rollbackResult.reason());
            } else {
                io:println("Rollback success");
            }
        } else {
            a = a + " Commit";
            boolean isFailed = transactions:getAndClearFailure();
            if (!isFailed) {
                var endSuccess = trap transactions:endTransaction(transactionId, transactionBlockId);
                if (endSuccess is string) {
                    if (endSuccess == transactions:OUTCOME_COMMITTED) {
                        io:println("Commit success");
                    }
                }
            }
        }
        a = a + " endTrx";
        a = (a + " end");
    };
    var result = trap trxFunc();
    if (result is error) {
    	 // may be we can try retry logic here;
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