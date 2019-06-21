import ballerina/io;

// Defines a record to represent the error details.
// This record can have fields of `anydata|error`types.
type AccountNotFoundErrorData record {
    int accountID;
};

// User-defined `error` types can be introduced by specifying a `reason` type-descriptor
// and optionally a `detail` type-descriptor.
// The `reason` type descriptor should be a subtype of `string` and the `detail`
// type descriptor should be a subtype of `record {}` or `map<anydata|error>`.
type AccountNotFoundError error<string, AccountNotFoundErrorData>;

function getAccountBalance(int accountID) returns int|AccountNotFoundError {
    // Returns an error if the `accountID` is less than zero.
    if (accountID < 0) {
        string errorReason = "Account Not Found";
        AccountNotFoundErrorData errorDetail = {
            accountID: accountID
        };
        AccountNotFoundError accountNotFoundError =
                                            error(errorReason, errorDetail);
        return accountNotFoundError;
    }
    // Returns a value if the `accountID` is greater than zero.
    return 600;
}

public function main() {
    var result = getAccountBalance(-1);
    // If the `result` is an `int`, then print the value.
    if (result is int) {
        io:println("Account Balance: ", result);
    // If an error is returned, print the reason and the account ID from the detail map.
    // The `.reason()` and `.detail()` built-in methods can be called on variables of
    // the type `error` to retrieve the reason and details of the error.
    } else {
        io:println("Error: ", result.reason(),
                   ", Account ID: ", result.detail().accountID);
    }
}
