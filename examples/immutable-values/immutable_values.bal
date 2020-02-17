import ballerina/io;

public function main() {
    // Create an `anydata`-typed `map` with two entries.
    map<string|int> m1 = {stringVal: "str", intVal: 1};

    // Call `.cloneReadOnly()` on the map `m1` and assign the returned value to another variable.
    map<string|int> m2 = m1.cloneReadOnly();

    // Reference equality checks for `m1` and `m2` should evaluate to false due to a clone being created
    // since `m1` is not an immutable value.
    io:println("m1 === m2: ", m1 === m2);

    // Check if `m1` is immutable. This evaluates to false since no changes are done to
    // the original value.
    io:println("m1 is immutable: ", m1.isReadOnly());

    // Check if `m2` is immutable. This evaluates to true since the returned clone is
    // marked as immutable.
    io:println("m2 is immutable: ", m2.isReadOnly());

    // Attempt to add an entry to the `map` and trap the panic if it results in a panic.
    error? updateResult = trap addEntryToMap(m2, "intValTwo", 10);
    if (updateResult is error) {
        // An error should occur since `m2` is frozen.
        io:println("Error occurred on update: ",
                   <string>updateResult.detail()?.message);
    }

    // Now call `.cloneReadOnly()` on the immutable value `m2`.
    map<string|int> m3 = m2.cloneReadOnly();

    // Reference equality checks for `m2` and `m3` should evaluate to true due to no clones being created
    // since `m2` is already an immutable value.
    io:println("m2 === m3: ", m2 === m3);

    // An `is` check for a frozen value becomes an `is like` check.
    // In other words, storage type is not considered.
    // Define a `map` of the constraint type `string` or `int`, but with
    // values of the type `string` only.
    map<string|int> m5 = {valueType: "map", constraint: "string"};
    // Make the map immutable. The resultant value would only
    // contain values of the type `string` and no values can now be
    // added to the map.
    var frozenVal = m5.cloneReadOnly();
    // Checking if the frozen value is of the type `map<string>` thus
    // evaluates to `true`.
    if (frozenVal is map<string>) {
        io:println("frozenVal is map<string>");
    }
}

// Function to add an entry to a `map`.
function addEntryToMap(map<string|int> m, string key, string|int value) {
    m[key] = value;
}
