import ballerina/io;
// Import transactions module in order to use participant annotation.
import ballerina/transactions;


public function main() {
    // `transaction` blocks initiates the transaction, it's called the initiator.
    transaction {
        // Invoke local participant.
        var res = trap localTransactionParticipant();
        if (res is error) {
            // local participant panicked.
            io:println("Local participant panicked.");
        }
    } onretry {
        io:println("Retrying transaction");
    } committed {
        io:println("Transaction committed");
    } aborted {
        io:println("Transaction aborted");
    }
}

// `@transactions:Participant` annotation from `transactions` package is used to indicate
// function as a local participant.
@transactions:Participant {
    oncommit: participantOnCommit
}
function localTransactionParticipant() {
    io:println("Invoke local participant function.");
    error er = error("Simulated Failure");
    panic er;
}

function participantOnCommit(string transactionId) {
    io:println("Local participant committed function handler...");
}
