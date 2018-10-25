import ballerina/io;
public function main(string... args) {
    int a = 10;

    transaction with retries = 1, oncommit = onCommitFunction, onabort = onAbortFunction {
        string testString = "Hello World!!";
        a = 1000;
    } onretry {
        io:println("Within On-Retry");
    }
}

function onCommitFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " committed");
}

function onAbortFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " aborted");
}
