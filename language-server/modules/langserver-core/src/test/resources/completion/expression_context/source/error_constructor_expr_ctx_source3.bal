import ballerina/module1;

type ErrorOne error<record { int code; }>;

type ErrorTwo error<record { string[] alternatives; }>;

public function main1() {
    error err = error 
}

function getStringVal() returns string {
    returns "Error!";
}
