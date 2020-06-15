## Module overview

This module provides Ballerina transaction manager implementation.

The code snippet given below uses the functions in the module to get the transaction information.

_NOTE: This module is deprecated from 2.0.0-preview1 onwards. Please use lang.transaction module instead._
```ballerina

// When the `transaction` statement starts, a distributed transaction context is created.
transaction {
    // Print the current transaction ID
    log:printInfo("Started transaction: " +
                     transactions:getCurrentTransactionId());

    // When a participant is called, the transaction context is propagated, and that participant
    // gets infected and joins the distributed transaction.
    boolean successful = callBusinessService();
    if (successful) {
        log:printInfo("Business operation executed successfully");
    } else {
        log:printError("Business operation failed");
        abort;
    }

    // As soon as the `transaction` block ends, the `2-phase commit
    // coordination` protocol will run. All participants are prepared
    // and depending on the joint outcome, either a `notify commit` or
    // `notify abort` will be sent to the participants.
} committed {
    log:printInfo("Initiated transaction committed");
} aborted {
    log:printInfo("Initiated transaction aborted");
}
```
