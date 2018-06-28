import ballerina/io;

type Record record {
    int id;
    string name;
};

// This example demonstrates throwing an error. The next example demonstrates how to catch the thrown errors.
function readRecord(Record|() value) {
    match value {
        Record rec => {
            io:println("Record ID: ", rec.id, ", value: ", rec.name);
        }
        (any|()) => {
            error err = { message: "Record is null" };
            throw err;
        }
    }
}
// Catch a thrown error. 
function main(string... args) {
    Record r1 = { id: 1, name: "record1" };
    readRecord(r1);
    Record|() r2;
    // Record r2 is null.
    match r2 {
        Record rec => {
            io:println("Record: " + rec.name);
        }
        (any|()) => {
            readRecord(r2);
        }
    }
    // The following lines of code will not be executed.
    Record r3 = { id: 3, name: "record3" };
    readRecord(r3);
}
