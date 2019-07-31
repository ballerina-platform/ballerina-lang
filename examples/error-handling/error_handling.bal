import ballerina/io;

const INVALID_ACC_TYPE_REASON = "InvalidAccountType";

type InvalidAccountTypeErrorData record {
    string message?;
    error cause?;
    string accountType;
};

// User-defined `error` with a `constant` as the reason.
type InvalidAccountTypeError error<INVALID_ACC_TYPE_REASON, InvalidAccountTypeErrorData>;

function getTypeId(string accountType) returns int | InvalidAccountTypeError {
    match accountType {
        "checking" => return 1;
        "savings" => return 2;
    }

    // When a constant reason is used in the error definition
    // it is not required to specify the reason when creating an error value of that type.
    // If a reason is passed, the value should be the constant used in the error definition.
    InvalidAccountTypeError e = error(accountType = accountType);
    return e;
}

// Define a record to represent the error details.
// This record can have fields of `anydata|error` types.
type AccountNotFoundErrorData record {
    string message?;
    error cause?;
    int accountID;
};

// User-defined `error` types can be introduced by specifying a `reason` type descriptor
// and optionally a `detail` type descriptor.
// The `reason` type descriptor should be a subtype of `string` and the `detail`
// type descriptor should be a subtype of 
// `record {| string message?; error cause?; (anydata|error)...; |}`.
const ACCOUNT_NOT_FOUND = "AccountNotFound";
const INVALID_ACCOUNT_ID = "InvalidAccountID";

type AccountNotFoundError error<ACCOUNT_NOT_FOUND | INVALID_ACCOUNT_ID, AccountNotFoundErrorData>;

function getAccountBalance(int accountID) returns int|AccountNotFoundError {
    // Values for the error detail mapping should be passed as named arguments.
    if (accountID < 0) {
        // Return an error with "InvalidAccountID" as the reason if the `accountID` is less than zero.
        AccountNotFoundError accountNotFoundError =
                                            error(INVALID_ACCOUNT_ID, accountID = accountID);
        return accountNotFoundError;
    } else if (accountID > 100) {
        // Return an error with "AccountNotFound" as the reason if the `accountID` is above hundred.
        AccountNotFoundError accountNotFoundError =
                                            error(ACCOUNT_NOT_FOUND, accountID = accountID);
    }
    // Return the value if the `accountID` is in between zero and hundred inclusive.
    return 600;
}

public function main() {
    // Define an error of the generic error type.
    error simpleError = error("SimpleErrorType", message = "Simple error occured");
    // Print the error reason and the `message` field from the error detail.
    // The `.reason()` and `.detail()` built-in methods can be called on variables of
    // the type `error` to retrieve the reason and details of the error.
    // `message` is an optional field in the generic error `Detail` record.
    io:println("Error: ", simpleError.reason(),
                ", Message: ", simpleError.detail()?.message);

    var result = getTypeId("Joined");
    if (result is int) {
        io:println("Account type ID: ", result);
    } else {
        io:println("Error: ", result.reason(),
                   ", Account type: ", result.detail().accountType);
    }

    var result2 = getAccountBalance(-1);
    // If the `result` is an `int`, then print the value.
    if (result2 is int) {
        io:println("Account Balance: ", result2);
    // If an error is returned, print the reason and the account ID from the detail record.
    } else {
        io:println("Error: ", result2.reason(),
                    ", Account ID: ", result2.detail().accountID);
    }
}
