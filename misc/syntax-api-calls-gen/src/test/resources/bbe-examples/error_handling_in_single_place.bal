import ballerina/io;

type InvalidAccountIDError error;

type AccountNotFoundError error;

const ACCOUNT_NOT_FOUND = "AccountNotFound";

const INVALID_ACCOUNT_ID = "InvalidAccountID";

public function main() {
    int negResult = getAccountBalance(-1);
    int invalidResult = getAccountBalance(200);

    transaction {
        int parsedNum = check parse("12");

        //Parsing a random string will return an error.
        //This error will be caught within the `on-fail` clause.
        int parsedStr = check parse("invalid");

        var res = commit;
    }
}

function parse(string num) returns int|error {
    return 'int:fromString(num);
}

function getAccountBalance(int accountID) returns int {
       do {
         if (accountID < 0) {
             // Throw an error with the `InvalidAccountID` as the reason if
             //the `accountID` is less than zero.
             InvalidAccountIDError invalidAccoundIdError
             = InvalidAccountIDError(INVALID_ACCOUNT_ID, accountID = accountID);
             fail invalidAccoundIdError;
         } else if (accountID > 100) {
             // Throw an error with the `AccountNotFound` as the reason if
             //the `accountID` is greater than hundred.
             AccountNotFoundError accountNotFoundError
             = AccountNotFoundError(ACCOUNT_NOT_FOUND, accountID = accountID);
             fail accountNotFoundError;
         }
       //The type of `e` should be the union of the error types that are
       //thrown by the `do` statement.
       } on fail InvalidAccountIDError|AccountNotFoundError e {
          io:println("Error caught: ", e.message(),", Account ID: ",
                     e.detail()["accountID"]);
       }
       return 600;
}
