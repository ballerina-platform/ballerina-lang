import ballerina/io;

// This function expects the `value` parameter to be either a `string` or an `int`.
function println(string|int value) {
    io:println(value);
}

// This function returns either a `string` or an `error`.
function getValue(string key) returns string|error {
    if (key == "") {
        error err = error("key '" + key + "' not found");
        return err;
    } else {
        return "this is a value";
    }
}

public function main() {
    // This passes a `string` value.
    println("This is a string");

    // This passes an `int` value.
    println(101);

    // This function call returns a `string` value.
    string|error valueOrError1 = getValue("name");
    io:println(valueOrError1);

    // This call returns an error.
    string|error valueOrError2 = getValue("");
    io:println(valueOrError2);
}
