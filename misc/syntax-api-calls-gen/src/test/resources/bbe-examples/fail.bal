import ballerina/io;

const ACCOUNT_NOT_FOUND = "AccountNotFound";

const INVALID_ACCOUNT_ID = "InvalidAccountID";

public function main() {
    int|error negResult = getAccountBalance(-1);
    if (negResult is error) {
        io:println("Error returned:", negResult.message());
    }

    int|error invalidResult = getAccountBalance(200);
    if (invalidResult is error) {
        io:println("Error returned:", invalidResult.message());
    }
}

function getAccountBalance(int accountID) returns int|error {
     if (accountID < 0) {
         // Creates an error with the `InvalidAccountID` as the reason if
         //the `accountID` is less than zero.
         error invalidAccoundIdError = error(INVALID_ACCOUNT_ID,
                                             accountID = accountID);
         // Returns the error. The error returned by the `fail` statement should match with the enclosing function's return type.
         fail invalidAccoundIdError;
     } else if (accountID > 100) {
         // Returns an error with the `AccountNotFound` as the reason if
         //the `accountID` is greater than hundred.
         error accountNotFoundError = error(ACCOUNT_NOT_FOUND,
                                            accountID = accountID);
         fail accountNotFoundError;
     }
     return 600;
}
