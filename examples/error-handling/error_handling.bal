import ballerina/io;

// Define a record to represent error details.
type AccountNotFoundErrorData record {
    int accountID;
};

// User defined `error` types can be introduced based on the `error` basic type.
// The reason type descriptor (a subtype of `string`) and optionally a `detail`
// type descriptor (a subtype of `record {}` or `map<anydata>`) need to be specified.
type AccountNotFoundError error<string, AccountNotFoundErrorData>;

function getAccountBalance(int accountID) returns int|AccountNotFoundError {
    // Return an error if the `accountID` is less than `0`.
    if (accountID < 0) {
        string errorReason = "Account Not Found";
        AccountNotFoundErrorData errorDetail = {
            accountID: accountID
        };
        AccountNotFoundError accountNotFoundError =
                                            error(errorReason, errorDetail);
        return accountNotFoundError;
    }
    // Return a value if the `accountID` is greater than `0`.
    return 600;
}

public function main() {
    var result = getAccountBalance(-1);
    // If `result` is an `int`, print the value.
    if (result is int) {
        io:println("Account Balance: ", result);
    // If an error is returned, print the reason and the account ID from the detail map.
    // The `.reason()` and `.detail()` built-in methods can be called on `error` typed
    // variables, to retrieve the error reason and details.
    } else {
        io:println("Error: ", result.reason(),
                   ", Account ID: ", result.detail().accountID);
    }
}
