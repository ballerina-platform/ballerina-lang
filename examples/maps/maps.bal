import ballerina/io;

public function main() {
    // Declares a `map` constrained by the type `string`.
    map<string> m;

    // You can also declare and initialize a `map` with a map literal.
    map<string> addrMap = {
        line1: "No. 20",
        line2: "Palm Grove",
        city: "Colombo 03",
        country: "Sri Lanka"
    };
    io:println(addrMap);

    // Retrieves a value using member access.
    string country = <string>addrMap["country"];
    io:println(country);

    // Retrieves a value using the `.get()` method.
    // Panics if the map does not have a member with the specified key.
    string line2 = addrMap.get("line2");
    io:println(line2);

    // The `.hasKey()` function checks if a map contains a specified key.
    boolean hasPostalCode = addrMap.hasKey("postalCode");
    io:println(hasPostalCode);

    // Adds or updates the value of a key.
    addrMap["postalCode"] = "00300";
    io:println(addrMap);

    // The `keys()` method returns the keys of the map as an array.
    io:println(addrMap.keys());

    // Prints the number of mappings in the `map`.
    io:println(addrMap.length());

    // Mappings can be removed using the `.remove()` method.
    string removedElement = addrMap.remove("postalCode");
    io:println(addrMap);

    // Maps support functional iteration.
    addrMap.forEach(function (string value) {
        io:println(value);
    });

    map<int> marks = {sam: 50, jon: 60};
    // Calling the `.entries()` method on a map will return the key (`string`) and the value as a tuple variable.
    map<int> modifiedMarks = marks.entries().map(function ([string, int] pair) returns int {
        var [name, score] = pair;
        io:println(io:sprintf("%s scored: %d", name, score));
        return score + 10;
    });
    io:println(modifiedMarks);
    
    // Maps can only contain values of the type specified by the constraint type descriptor.
    map<string> stringMap = {};
    stringMap["index"] = "100892N";

    // The return type of member access will be `T?` where `T` is the constraint type of the map.
    // If the key does not exist, nil (`()`) is returned.
    // The Elvis operator `?:` is a conditional operator that handles `()`.
    // If the given expression evaluates to nil, the second expression is evaluated and its value is returned.
    string index2 = stringMap["index"] ?: "";
    io:println(index2);
}
