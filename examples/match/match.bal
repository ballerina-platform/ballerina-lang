import ballerina/io;

type KeyNotFoundError error<string, record { string key; }>;

// The values of this map are constrained to the values of the optional string type.
map<string?> values = {"key1": "value1", "key2": ()};

// This function returns either a `string`, a `KeyNotFoundError` or nil.
function getValue(string key) returns string?|KeyNotFoundError {
    if (!values.hasKey(key)) {
        KeyNotFoundError err = error("key '" + key + "' not found", { key: key });
        return err;
    } else {
        return values[key];
    }
}

// This function prints a custom message depending on the type of the result.
function print(string?|KeyNotFoundError result) {
    // This type-guard check (in other word, this is `is` check) which checks the type of the reference variable. Inside
    // the if block, the refernce variable is cast to the respective `is` checked type.
    if (result is string) {
        io:println("value: " + result);
    } else if (result is ()) {
        io:println("value is ()");
    } else if (result is KeyNotFoundError) {
        io:println(result.reason());
    }
}

public function main() {
    print(getValue("key1"));
    print(getValue("key2"));
    print(getValue("key3"));
}
