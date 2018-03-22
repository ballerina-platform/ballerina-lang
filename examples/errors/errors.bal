import ballerina/io;

@Description {value:"As a best practice, error will be the last return value. Its type should be 'error', a built-in reference type."}
function getAccountBalance (int accountID) returns (int) | error {
    //Here we create an instance of the error struct and return.
    //This logic is used only to explain the concept.
    if (accountID < 100) {
        error err = {message:"Account with id:" + accountID + " is not found"};
        return err;
    } else {
        return 600;
    }
}

function main (string[] args) {
    // Best practice is to check whether an error has occurred.
    var r = getAccountBalance(24);

    match r {
        int blnc => {
            io:println(blnc);
        }
        error err => {
            io:println("Error occurred: " + err.message);
        }
    }
}
