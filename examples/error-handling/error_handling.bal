import ballerina/io;

type AccountNotFoundErrorData record {
    string accountID;
};

// Extended error types can be declared from `error` type. Thus `error` can be extended for single level.
type AccountNotFoundError error<string, AccountNotFoundErrorData>;

// Note that type `error` is a built-in reference type.
function getAccountBalance(int accountID) returns (int|AccountNotFoundError) {
    // Create an instance of the error record and return it.
    // The logic used here is a sample to demonstrate the concept of error handling.
    if (accountID < 100) {
        AccountNotFoundError accountNotFoundError = error("Account with ID: "
                                        + accountID + " is not found",
                                        { accountID: <string>accountID });
        return accountNotFoundError;
    } else {
        return 600;
    }
}

public function main() {
    // As a best practice, check whether an error occurrs.
    var r = getAccountBalance(24);

    // Type-guard check (`is` check) for variable type checking.
    if (r is int) {
        io:println(r);
    } else {
        // `error` type has 3 functions `reason()`, `detail()` and 'stacktarce()'. Only `reason()` can be set from construtor.
        io:println("Error occurred: ", r.reason(), ", accountID: ",
                        r.detail().accountID);
    }
}
