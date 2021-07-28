import ballerina/module1;

type ErrorOne error<record { int code; }>;

type ErrorTwo error<record { string[] alternatives; }>;

public function main1() {
    error err = error Er
}

function getStringVal() returns string {
    return "Error!";
}

type ErrorThree ErrorOne;
