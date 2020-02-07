import ballerina/io;

type Record record {
    int id;
    string name;
};

// This example demonstrates how `panic` works.
function readRecord(Record? value) {
    if (value is Record) {
        io:println("Record ID: ", value.id, ", value: ", value.name);
    } else {
        error err = error("Record is nil");
        panic err;
    }
}

function divide(int a, int b) returns int {
    return a / b;
}

public function main() {
    int | error result = trap divide(1, 0);

    if (result is int) {
        io:println("int result: " + result);
    } else {
        io:println("Error occurred: ", result.reason());
        panic result;
    }
}

function throwError(error err) {
    panic err;
}

function throwError2(int | error a) {
    int b = 0;
    if (a is int) {
        b += a;
    } else {
        panic
        a;
    }
}

public function main2(string... args) {
    panic error("Player has to be initialized");
}

public function main3(string... args) {
    panic
    error
    (
    "Player has to be initialized"
    )
    ;
}

public function main4(string... args) {
    panic getError();
}

public function getError() returns error {
    return error("Player has to be initialized");
}
