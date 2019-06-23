import ballerina/io;

// Defines a `Department` record that only has `anydata|error` typed fields.
// (A record with fields only of types `anydata` or `error` belongs to `anydata`)
type Department record {
    string name;
    int id;
};

// Defines an `Employee` object.
type Employee object {
    string name;

    function __init(string name) {
        self.name = name;
    }

    function getName() returns string {
        return self.name;
    }
};

public function main() {
    // Creates an `anydata`-typed `map` with two entries.
    map<string|int> m1 = { stringVal: "str", intVal: 1 };

    // Freezes the map `m1` and assigns the returned value to another variable.
    // Since the map is of type `anydata`, the return value would be of the same type as `m1`.
    map<string|int> m2 = m1.freeze();

    // Reference equality checks for `m1` and `m2` should evaluate to true, since the same value is returned with the
    // frozen flag set.
    io:println("m1 === m2: ", m1 === m2);

    // Checks if `m1` is frozen.
    io:println("Frozen status of m1: ", m1.isFrozen());

    // Attempts to add an entry to the `map`, and `trap` the panic if it results in a panic.
    error? updateResult = trap addEntryToMap(m2, "intValTwo", 10);
    if (updateResult is error) {
        // An error should occur since `m2` is frozen.
        io:println("Error occurred on update: ",
                   <string>updateResult.detail().message);
    }

    // Creates a `Department` record.
    Department d = { name: "finance", id: 1100 };

    // Creates a `map` that may hold `anydata|error`-typed values.
    // (`map<anydata|error>` belongs to `anydata`)
    map<any> m3 = { stringVal: "str", intVal: 1, recVal: d };

    // Attempts to freeze `m3`. The return type could now be an `error` since there is the possibility that a
    // `map` constrained by the type `any` could have non-anydata values.
    map<any>|error freezeResult = m3.freeze();
    if (freezeResult is error) {
        io:println("'.freeze()' failed for m3: ",
                   <string>freezeResult.detail().message);
    } else {
        io:println("'.freeze()' successful for m3");
    }

    // Creates an `Employee` object.
    Employee e = new("Anne");

    // Now, creates a `map` that may hold `anydata|error` values, and add the non-anydata object `Employee` too.
    map<any> m4 = { stringVal: "str", intVal: 1, objVal: e };

    // Attempts to freeze `m4`.
    freezeResult = m4.freeze();
    if (freezeResult is error) {
        io:println("'.freeze()' failed for m4: ",
                   <string>freezeResult.detail().message);
    } else {
        io:println("'.freeze()' successful for m4");
    }

    // An `is` check for a frozen value becomes an `is like` check.
    // In other words, storage type is not considered.
    // Defines a `map` of the constraint type `string` or `int`, but with
    // values of the type `string` only.
    map<string|int> m5 = { valueType: "map", constraint: "string" };
    // Freezes the `map`. The `.freeze()` attempt will be successful
    // since the constraint is `anydata`. The frozen `map` only
    // contains values of the type `string`.
    var frozenVal = m5.freeze();
    // Checking if the frozen value is of the type `map<string>` would
    // evaluate to `true`.
    if (frozenVal is map<string>) {
        io:println("frozenVal is map<string>");
    }
}

// Function to add an entry to a `map`.
function addEntryToMap(map<string|int> m, string key, string|int value) {
    m[key] = value;
}
