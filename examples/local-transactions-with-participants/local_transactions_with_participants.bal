import ballerina/io;
// Import the `ballerina/transactions` module to use the
// `@transactions:Participant` annotation.
import ballerina/transactions;


public function main() {
    // The `transaction` block initiates the transaction. Thus, it is called the initiator.
    transaction {
        // Invokes the local participant.
        var res = trap localTransactionParticipant();
        if (res is error) {
            // The local participant gets panicked.
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

// The `@transactions:Participant` annotation from the
// `transactions` module is used to indicate the function as a local participant.
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
