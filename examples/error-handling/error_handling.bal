import ballerina/io;

// Define a record to represent the error details.
type AccountNotFoundErrorData record {
    int accountID;
};

// User-defined `error` types can be introduced based on the `error` type.
// The `reason` type descriptor (a subtype of `string`), and optionally a `detail`
// type descriptor (a subtype of `record {}` or `map<anydata>`) need to be specified.
type AccountNotFoundError error<string, AccountNotFoundErrorData>;

function getAccountBalance(int accountID) returns int|AccountNotFoundError {
    // Return an error if the `accountID` is less than zero.
    if (accountID < 0) {
        string errorReason = "Account Not Found";
        AccountNotFoundErrorData errorDetail = {
            accountID: accountID
        };
        AccountNotFoundError accountNotFoundError =
                                            error(errorReason, errorDetail);
        return accountNotFoundError;
    }
    // Return a value if the `accountID` is greater than zero.
    return 600;
}

public function main() {
    var result = getAccountBalance(-1);
    // If the `result` is an `int`, then print the value.
    if (result is int) {
        io:println("Account Balance: ", result);
    // If an error is returned, print the reason and the account ID from the detail map.
    // The `.reason()` and `.detail()` built-in methods can be called on variables of
    // type `error` to retrieve the reason and details of the error.
    } else {
        io:println("Error: ", result.reason(),
                   ", Account ID: ", result.detail().accountID);
    }
}
