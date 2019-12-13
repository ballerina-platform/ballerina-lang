import ballerina/io;

const INVALID_ACC_TYPE = "InvalidAccountType";

// Define a record to represent the error details.
// This record can have fields of `anydata|error` types and should be a subtype of the built-in error's detail type.
type InvalidAccountTypeErrorData record {
    string message?;
    error cause?;
    string accountType;
};

// User-defined `error` with a `constant` reason.
type InvalidAccountTypeError error<INVALID_ACC_TYPE, InvalidAccountTypeErrorData>;

function getTypeId(string accountType) returns int | InvalidAccountTypeError {
    match accountType {
        "checking" => { return 1; }
        "savings" => { return 2; }
    }

    // When a constant reason is used in the error definition the error type name can be used as the error constructor,
    // and the error details can be provided as named arguments, without specifying the reason.
    InvalidAccountTypeError e = InvalidAccountTypeError(accountType = accountType);
    return e;
}

type AccountNotFoundErrorData record {
    string message?;
    error cause?;
    int accountID;
};

const INVALID_ACCOUNT_ID = "InvalidAccountID";
const ACCOUNT_NOT_FOUND = "AccountNotFound";

// Define an `error` type where error reason must be either `ACCOUNT_NOT_FOUND` or `INVALID_ACCOUNT_ID`.
type AccountNotFoundError error<ACCOUNT_NOT_FOUND | INVALID_ACCOUNT_ID, AccountNotFoundErrorData>;

function getAccountBalance(int accountID) returns int|AccountNotFoundError {
    if (accountID < 0) {
        // Return an error with "InvalidAccountID" as the reason if the `accountID` is less than zero.
        // The default error constructor can be used to construct the error value.
        AccountNotFoundError accountNotFoundError =
                                            error(INVALID_ACCOUNT_ID, accountID = accountID);
        return accountNotFoundError;
    } else if (accountID > 100) {
        // Return an error with "AccountNotFound" as the reason if the `accountID` is greater than hundred.
        AccountNotFoundError accountNotFoundError =
                                            error(ACCOUNT_NOT_FOUND, accountID = accountID);
        return accountNotFoundError;
    }
    // Return a value if the `accountID` is in between zero and hundred inclusive.
    return 600;
}

// Error detail type where `message` and `cause` are mandatory.
type InquiryFailedErrorData record {|
    string message;
    error cause;
    int accountID;
|};

type AccountInquiryFailed error<string, InquiryFailedErrorData>;

function transferToAccount(int fromAccountId, int toAccountId, int amount) returns int|AccountInquiryFailed {
    var balance = getAccountBalance(fromAccountId);
    if (balance is error) {
        // Create a new error, with the error returned from `getAccountBalance()` as the cause.
        AccountInquiryFailed e = error("AccountInquiryFailed", message = balance.reason(), cause = balance, accountID = fromAccountId);
        return e;
    } else {
        // Perform transfer
    }

    return 0;
}

public function main() {
    int|InvalidAccountTypeError result = getTypeId("Joined");
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

    var result3 = transferToAccount(-1, 90, 1000);
    if (result3 is int) {
        io:println("Transfer success: ", result3);
    } else {
        // Print the mandatory error detail fields message and cause.
        io:println("Error: ", result3.reason(),
                    ", Message: ", result3.detail().message,
                    ", Cause: ", result3.detail().cause);
    }
}
