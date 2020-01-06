import ballerina/io;

const ACCOUNT_NOT_FOUND = "AccountNotFound";
const INVALID_ACCOUNT_ID = "InvalidAccountID";

function getAccountBalance(int accountID) returns int|error {
    if (accountID < 0) {
        // Return an error with "InvalidAccountID" as the reason if `accountID` is less than zero.
        return error(INVALID_ACCOUNT_ID, accountID = accountID);
    } else if (accountID > 100) {
        // Return an error with "AccountNotFound" as the reason if `accountID` is greater than hundred.
        return error(ACCOUNT_NOT_FOUND, accountID = accountID);
    }
    // Return a value if `accountID` is in between zero and hundred inclusive.
    return 600;
}

public function main() {
    // Define an error of the generic `error` type using the default error constructor.
    // The error reason must be the first and only positional argument to the default error constructor and the type of the 
    // value must be a subtype of `string`. Additional fields providing more details can be passed as named arguments to the constructor
    // and the type of each of those must be a subtype of `anydata|error`.
    error simpleError = error("SimpleErrorType", message = "Simple error occurred");

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
    // Each additional error detail field provided to the error constructor is available as a field in the error detail record.
    } else {
        io:println("Error: ", result.reason(),
                    ", Account ID: ", result.detail()["accountID"]);
    }
}
