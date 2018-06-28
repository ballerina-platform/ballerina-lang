import ballerina/io;

// Note that type `error` is a built-in reference type.
function getAccountBalance(int accountID) returns (int|error) {
    // Create an instance of the error record and return it.
    // The logic used here is a sample to demonstrate the concept of error handling.
    if (accountID < 100) {
        error err = { message: "Account with ID: " + accountID +
            " is not found" };
        return err;
    } else {
        return 600;
    }
}

function main(string... args) {
    // As a best practice, check whether an error occurrs.
    var r = getAccountBalance(24);

    match r {
        int blnc => {
            io:println(blnc);
        }
        error err => {
            io:println("Error occurred: " + err.message);
        }
    }
}
