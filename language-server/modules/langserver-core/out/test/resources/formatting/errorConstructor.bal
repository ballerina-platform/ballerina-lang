import ballerina/io;

type AccountNotFoundErrorData record {
    string accountID;
};

type AccountNotFoundError error<string, AccountNotFoundErrorData>;

function getAccountBalance1(int accountID) returns (int | AccountNotFoundError) {
    if (accountID < 100) {
        AccountNotFoundError accountNotFoundError =   error  (   "Account with ID: "+ accountID + " is not found"  ,{
        accountID: <string>accountID } );
        return accountNotFoundError;
    } else {
        return 600;
    }
}

function getAccountBalance2(int accountID) returns (int | AccountNotFoundError) {
    if (accountID < 100) {
        AccountNotFoundError accountNotFoundError =error  (   "Account with ID: "+ accountID + " is not found" ,
        {
        accountID: <string>accountID } );
        return accountNotFoundError;
    } else {
        return 600;
    }
}

function getAccountBalance3(int accountID) returns (int | AccountNotFoundError) {
    if (accountID < 100) {
        AccountNotFoundError accountNotFoundError =
                error
   (
                    "Account with ID: " + accountID + " is not found"
                  ,
                       {
                  accountID: <string>accountID }
  )
                ;
        return accountNotFoundError;
    } else {
        return 600;
    }
}