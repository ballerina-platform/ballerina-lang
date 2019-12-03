import ballerina/io;

const ACCOUNT_NOT_FOUND = "AccountNotFound";
const INVALID_ACCOUNT_ID = "InvalidAccountID";

function getAccountBalance(int accountID) returns int|error {
    if (accountID < 0) {
        return error(INVALID_ACCOUNT_ID, accountID = accountID);
    } else if (accountID > 100) {
        // Return an error with "AccountNotFound" as the reason if the `accountID` is above hundred.
        return error(ACCOUNT_NOT_FOUND, accountID = accountID);
    }
    // Return the value if the `accountID` is in between zero and hundred inclusive.
    return 600;
}

public function main() {
    // Define an error of the generic error type.
    // `error` is the default error type and the default error constructor.
    // Error reason must be the first and only positional argument to default error constructor and the error reason
    // value must be a subtype of string. Additional error detail can be provided as named argument to the constructor
    // and each of those must be subtype of `anydata|error`.
    error simpleError = error("SimpleErrorType", message = "Simple error occured");

    // Print the error reason and the `message` field from the error detail.
    // The `.reason()` and `.detail()` methods can be invoked on error values
    // to retrieve the reason and details of the error.
    // `message` is an optional field in the generic error `Detail` record.
    io:println("Error: ", simpleError.reason(),
                ", Message: ", simpleError.detail()?.message);

    int|error result = getAccountBalance(-1);
    // If the `result` is an `int`, then print the value.
    if (result is int) {
        io:println("Account Balance: ", result);
    // If an error is returned, print the reason and the account ID from the detail record.
    // Each additional error detail provided to the error constructor is stored as a field in the error detail record.
    } else {
        io:println("Error: ", result.reason(),
                    ", Account ID: ", result.detail()["accountID"]);
    }
}
