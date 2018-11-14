import ballerina/io;

// This function expects the `value` parameter to be either a `string` or an `int`.
function println(string|int value) {
    io:println(value);
}

// This is a custom error record.
type KeyNotFoundError error<string, record { string key; }>;

// This function returns either a `string` or a `KeyNotFoundError`.
function getValue(string key) returns string|KeyNotFoundError {
    if (key == "") {
        KeyNotFounderror err = error("key '" + key + "' not found", { key: key });
        return err;
    } else {
        return "this is a value";
    }
}

public function main() {
    // This passes a string value.
    println("This is a string");

    // This passes an int value.
    println(101);

    // This function call returns a string value.
    string|KeyNotFoundError valueOrError1 = getValue("name");
    io:println(valueOrError1);

    // This call returns an error.
    string|KeyNotFoundError valueOrError2 = getValue("");
    io:println(valueOrError2);
}
