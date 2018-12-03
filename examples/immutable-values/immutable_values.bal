import ballerina/io;

type Department record {
    string name;
    int id;
};

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
    // define an `anydata` typed `map` with two entries
    map<string|int> m1 = { stringVal: "str", intVal: 1 };

    // `freeze` the map `m1` and assign the returned value to another variable.
    // Since the map is of type `anydata`, the return value would be of the same type as `m1`
    map<string|int> m2 = m1.freeze();

    // reference equality checks for `m1` and `m2` should evaluate to true, since the same value is returned with the
    // frozen flag set
    io:println("m1 === m2: ", m1 === m2);

    // check if `m1` is frozen
    io:println("frozen status of m1: ", m1.isFrozen());

    // attempt adding an entry to the map, and trap the error if an error occurs
    error? updateResult = trap addEntryToMap(m2, "intValTwo", 10);
    if (updateResult is error) {
        // an error should occur since `m2` is frozen
        io:println("error occurred on update: ", updateResult.reason());
    }

    // define a `Department` record
    Department d = { name: "finance", id: 1100 };

    // define a map that may hold anydata values
    map<any> m3 = { stringVal: "str", intVal: 1, recVal: d };

    // attempt freezing `m3`. Note how the return type could now be an error, since there is the possibility that a
    // `map` constrained by type `any` could have `non-anydata` values.
    map<any>|error freezeResult = m3.freeze();
    if (freezeResult is error) {
        io:println("'freeze()' failed for m3: ", freezeResult.reason());
    } else {
        io:println("'freeze()' successful for m3");
    }

    // define an `Employee` object
    Employee e = new("Anne");

    // now, define a map that may hold anydata values, and add the `non-anydata` object `Employee` too
    map<any> m4 = { stringVal: "str", intVal: 1, objVal: e };

    // attempt freezing `m4`
    freezeResult = m4.freeze();
    if (freezeResult is error) {
        io:println("'freeze()' failed for m4: ", freezeResult.reason());
    } else {
        io:println("'freeze()' successful for m4");
    }
}

function addEntryToMap(map<string|int> m, string key, string|int value) {
    m[key] = value;
}
