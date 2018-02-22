import ballerina.io;

struct Record {
    int id;
    string name;
}

@Description {value:"Here's how you can throw an error. Next example shows you how to catch thrown errors."}
function readRecord (Record value) {
    if (value == null) {
        error err = {message:"Record is null"};
        throw err;
    }
    io:println("Record ID: " + value.id + ", value: " + value.name);
}

function main (string[] args) {
    Record r1 = {id:1, name:"record1"};
    readRecord(r1);
    Record r2;
    // Record r2 is null.
    readRecord(r2);
    // Following lines will not execute.
    Record r3 = {id:3, name:"record3"};
    readRecord(r3);
}
