import ballerina/io;

@Description {value:"As a best practice, ensure that error is the last return value, and also ensure that it is of type `error`. Note that type `error` is a built-in reference type."}
function getAccountBalance (int accountID) returns (int) | error {
    //Create an instance of the error struct and return it.
    //The logic used here is just sample logic to demonstrate the concept of error.
    if (accountID < 100) {
        error err = {message:"Account with id:" + accountID + " is not found"};
        return err;
    } else {
        return 600;
    }
}

function main (string[] args) {
    // As a best practice, check whether an error occurrs.
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
