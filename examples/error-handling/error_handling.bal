import ballerina/io;

// Note that type `error` is a built-in reference type.
function getAccountBalance(int accountID) returns (int|error) {
    // Create an instance of the error record and return it.
    // The logic used here is a sample to demonstrate the concept of error handling.
    if (accountID < 100) {
        error err = error("Account with ID: " + accountID + " is not found");
        return err;
    } else {
        return 600;
    }
}

public function main() {
    // As a best practice, check whether an error occurrs.
    var r = getAccountBalance(24);

    // Type guard check (`is` check) for variable type checking.
    if (r is int) {
        io:println(r);
    } else if (r is error) {
        io:println("Error occurred: " + r.reason());
    }
}
