@Description {value:"As a best practice, error will be the last return value. It's type should be 'errors:Error', a built-in reference type."}
function getAccountBalance (int accountID) (int, error) {
    //Here we create an instance of the error struct and return.
    //This logic is used only to explain the concept.
    if (accountID < 100) {
        error err = {msg:"Account with id:" + accountID + " is not found"};
        return 0, err;
    } else {
        return 600, null;
    }
}

function main (string[] args) {
    // Best practice is to check whether an error has occurred.
    var balance, err = getAccountBalance(23);
    if (err != null) {
        println("error: " + err.msg);
    }
}
