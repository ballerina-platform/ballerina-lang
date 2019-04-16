import ballerina/io;

type Record record {
    int id;
    string name;
};

function readRecord(Record? value) {
    if (value is Record) {
         io:println("Record ID: ", value.id, ", value: ", value.name);
    } else {
        // Panic if `value` is `()`.
        error err = error("Record is nil");
        panic err;
    }
}

public function main() {
    Record r1 = { id: 1, name: "record1" };
    readRecord(r1);
    Record? r2 = ();
    // Record r2 is `nil`.
    if (r2 is Record) {
        io:println("Record: " + r2.name);
    } else {
        // Since `r2` is `()` `readRecord()` would panic.
        readRecord(r2);
    }
    // The following lines of code will not be executed.
    Record r3 = { id: 3, name: "record3" };
    readRecord(r3);
}
