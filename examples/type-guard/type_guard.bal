import ballerina/io;

type Person record {
    string name;
    Address? address;
    Employment? employment;
};

type Address record {
    string street;
    string city;
};

type Employment record {
    string company;
    string position;
};

function typeTestDemo(Person p) returns (Address?|Employment|error) {

    // Person's employment is assigned to simple variable reference.
    Employment? emp = p.employment;
    if (p.address is Address) {
        // This is a type-guard which tests for `Address` type for `p.address` field from `Person` record.
        return p.address;
    } else if (emp is Employment) {
        // This is a type-guard, which tests for `Employment` type, thus the type of `emp` variable will be `Employment`
        // within this if-block.
        return emp;
    } else {
        error addNotFoundErr = error("address not found");
        return addNotFoundErr;
    }
}

type KeyNotFoundError error<string, record { string key; }>;

// The values of this map are constrained to the values of the optional string type.
map<string?> values = {"key1": "value1", "key2": ()};

// This function returns either a `string`, a `KeyNotFoundError` or nil.
function getValue(string key) returns string?|KeyNotFoundError {
    if (!values.hasKey(key)) {
        KeyNotFoundError err = error("key '" + key + "' not found",
                                     { key: key });
        return err;
    } else {
        return values[key];
    }
}

// This function prints a custom message depending on the type of the result.
function print(string?|KeyNotFoundError result) {
    // This type-guard check (also known as `is` check) which checks the type of the reference variable. Inside
    // the if block, the reference variable will be converted to the respective `is` checked type in the type guard.
    if (result is string) {
        // The type of the variable `result` will be `string` within this if-block
        io:println("value: " + result);
    } else if (result is ()) {
        // The type of the variable `result` will be `()` or `nil`
        io:println("value is ()");
    } else {
        // The type of the variable `result` will be `KeyNotFoundError`
        io:println(result.reason());
    }
}

public function main() {
    print(getValue("key1"));
    print(getValue("key2"));
    print(getValue("key3"));

    Person tom = { name: "tom", address: (),
                   employment: {company: "Ballerina", position: "CEO"} };
    Address?|Employment|error address = typeTestDemo(tom);
    io:println("Address: " + io:sprintf("%s", address));
}
