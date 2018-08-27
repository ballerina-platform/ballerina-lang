import ballerina/io;

function transactionTest() {
    transaction with retries = 1, oncommit = onCommitFunction, onabort = onAbortFunction {
        string testString = "Hello World!!";
        testString.contains(
    }
}

function onCommitFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " committed");
}

function onAbortFunction(string transactionId) {
    io:println("Transaction: " + transactionId + " aborted");
}