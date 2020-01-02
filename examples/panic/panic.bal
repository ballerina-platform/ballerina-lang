import ballerina/io;

type Record record {
    int id;
    string name;
};

function readRecord(Record? value) {
    if (value is Record) {
         io:println("Record ID: ", value.id, ", value: ", value.name);
    } else {
        // Panics if `value` is `()`.
        error err = error("Record is nil");
        panic err;
    }
}

public function main() {
    Record r1 = {
        id: 1,
        name: "record1"
    };
    readRecord(r1);

    // `r2` is `()`.
    Record? r2 = ();
    // Since `r2` is `()`, `readRecord()` will panic.
    readRecord(r2);

    // The following lines of code will not be executed.
    Record r3 = {
        id: 3,
        name: "record3"
    };
    readRecord(r3);
}
