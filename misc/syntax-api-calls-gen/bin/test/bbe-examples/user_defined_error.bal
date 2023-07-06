import ballerina/io;

// Define a record to represent the error details.
// This record can have fields of `anydata|reaonly` types.
// i.e., it should be a subtype of `map<anydata|readonly>`.
type InvalidAccountTypeErrorData record {
    string accountType;
};

// User-defined `error` type.
type InvalidAccountTypeError error<InvalidAccountTypeErrorData>;

function getTypeId(string accountType) returns int|InvalidAccountTypeError {
    match accountType {
        "checking" => { return 1; }
        "savings" => { return 2; }
    }

    // To construct an error value of the user-defined error type
    // `InvalidAccountTypeError`, the type name is used as the error constructor.
    // The first argument to the error constructor is the error message,
    // and the error constructor takes a second optional argument as the error cause.
    // The error details can be provided as named arguments.
    return InvalidAccountTypeError("Invalid account type",
                                    accountType = accountType);
}

type AccountErrorData record {|
    int accountID;
|};

type AccountTransferErrorData record {|
    int fromAccountId;
    int toAccountId;
|};

// To distinctly identify different errors and handle them appropriately,
// distinct errors can be used.
// Distinct types are similar to nominal types and can be used to create
// distinct type hierarchies.
// `InvalidAccountIdError` and `AccountNotFoundError` are subtypes of AccountError.
type AccountError distinct error<AccountErrorData>;
type InvalidAccountIdError distinct AccountError;
type AccountNotFoundError distinct AccountError;

function getAccountBalance(int accountID) returns int|AccountError {
    if (accountID < 0) {
        // Return an `InvalidAccountIdError` if the `accountID` is less than zero.
        return InvalidAccountIdError("Invalid account Id",
                                      accountID = accountID);
    } else if (accountID > 100) {
        // Return an `AccountNotFoundError` if the `accountID` is greater than hundred.
        return AccountNotFoundError("Account not found", accountID = accountID);
    }
    // Return a value if the `accountID` is in between zero and hundred inclusive.
    return 600;
}

type AccountTransferError error<AccountTransferErrorData>;

function transferToAccount(int fromAccountId, int toAccountId, int amount)
                            returns AccountTransferError? {
    var result = getAccountBalance(fromAccountId);
    if (result is error) {
        // Create a new error with the error returned from `getAccountBalance()` as the cause.
        return AccountTransferError("Account transfer failed", result, 
                                    fromAccountId = fromAccountId,
                                    toAccountId = toAccountId);
    } else {
        // Perform transfer
    }
}

public function main() {
    int|InvalidAccountTypeError result = getTypeId("Joined");
    if (result is int) {
        io:println("Account type ID: ", result);
    } else {
        io:println("Error: ", result.message(),
                   ", Account type: ", result.detail().accountType);
    }

    var result2 = getAccountBalance(-1);
    // If the `result` is an `int`, then print the value.
    if (result2 is int) {
        io:println("Account Balance: ", result2);
    // Type test expressions can be used to identify `distinct` errors.
    } else if (result2 is InvalidAccountIdError) {
        io:println("Invalid account number: Please try again!");
    } else {
        io:println("Error: ", result2.message(),
                    ", Account ID: ", result2.detail().accountID);
    }

    var result3 = transferToAccount(-1, 90, 1000);
    if (result3 is error) {
        // Print the mandatory error detail fields (i.e., message and cause).
        io:println("Error: ", result3.message(),
                    ", Cause: ", result3.cause());
    } else {
        io:println("Transfer success");
    }
}
