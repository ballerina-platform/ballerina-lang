import ballerina/lang.'transaction as transactions;
import ballerina/io;

// Defines the rollback handlers, which are triggered once the
// rollback statement is executed.
function onRollbackFunc(transactions:Info info, error? cause,
                        boolean willRetry) {
    io:println("Rollback handler #1 executed.");
}

function onRollbackFunc2(transactions:Info info, error? cause,
                         boolean willRetry) {
    io:println("Rollback handler #2 executed.");
}

// Defines the commit handler, which gets triggered once the
// commit action is executed.
function onCommitFunc(transactions:Info info) {
    io:println("Commit handler executed.");
}

public function main() {
    // The `transaction` block initiates the transaction.
    transaction {
        // Register the rollback handler to the transaction context.
        // Multiple rollback handlers can be registered and they
        // are executed in reverse order.
        transactions:onRollback(onRollbackFunc);
        transactions:onRollback(onRollbackFunc2);

        // Register the commit handler to the transaction context.
        // Multiple commit handlers can be registered and they
        // are executed in reverse order.
        transactions:onCommit(onCommitFunc);

        // Returns information about the current transaction.
        transactions:Info transInfo = transactions:info();
        io:println("Transaction Info: ", transInfo);

        // Invokes the local participant.
        var res = erroneousOperation();
        if res is error {
            // The local participant execution fails.
            io:println("Local participant error.");
            rollback;
        } else {
            io:println("Local participant successfully executed.");
            var commitRes = commit;
        }
    }
}

function erroneousOperation() returns error? {
    io:println("Invoke local participant function.");
    return error("Simulated Failure");
}
