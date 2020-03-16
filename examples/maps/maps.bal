import ballerina/io;

public function main() {
    // Declare a `map` constrained by the type `string`.
    map<string> m;

    string city = "Colombo";
    string country = "Sri Lanka";
    string codeLiteral = "code";

    // You can also declare and initialize a `map` with a mapping constructor
    // expression.
    map<string> addrMap = {
        // A field can be specified as a key-value pair.
        line1: "No. 20",
        line2: "Palm Grove",
        // The value in a key-value pair can be any expression that evaluates
        // to a value of a type that belongs to the constraint type of the map.
        city: "Colombo 03",
        // A field can also be just a variable reference, which would result
        // in the variable name being the field name and the variable itself
        // being the value expression.
        // This is equivalent to saying `country: country`.
        country,
        // The key in a key-value pair can also be a computed key.
        // A computed key is defined by specifying the key expression within
        // brackets. The key expression must belong to type `string`.
        // For a computed key, the key expression is evaluated at runtime and
        // the resulting value is used as the key.
        [codeLiteral]: "00300"
    };
    io:println(addrMap);

    // Retrieve a value using member access.
    // Member access returns the value if a field exists in the map with the
    // specified key, or `()` if a field does not exist with the specified key.
    // The type of a member access expression for a map is thus the union of
    // the constraint type and the nil type.
    string? countryValue = addrMap["country"];

    if (countryValue is string) {
        io:println(countryValue);
    } else {
        io:println("key 'country' not found");
    }

    // Retrieve a value using the `.get()` method.
    // If the map has a field with the specified key, `.get()` returns the value,
    // or panics if the map does not have a field with the specified key.
    // Thus, the type for `.get()` on a map is the map's constraint type.
    string line2Value = addrMap.get("line2");
    io:println(line2Value);

    // The `.hasKey()` function checks if a map contains the specified key.
    boolean hasPostalCode = addrMap.hasKey("postalCode");
    io:println(hasPostalCode);

    // Member access lvalue expressions can be used to add fields to a map
    // or update an already existing field in the map.
    addrMap["postalCode"] = "00300";
    io:println(addrMap);

    // The `.keys()` method returns the keys of the map as an array.
    io:println(addrMap.keys());

    // Print the number of fields in the map.
    io:println(addrMap.length());

    // Fields can be removed using the `.remove()` method.
    string removedElement = addrMap.remove("code");
    io:println(addrMap);

    // Maps support functional iteration.
    addrMap.forEach(function (string value) {
        io:println(value);
    });

    map<int> marks = {sam: 50, jon: 60};

    // Calling the `.entries()` method on a map will return the key (`string`)
    // and the value pairs as tuples.
    map<int> modifiedMarks = marks.entries().map(function ([string, int] pair)
        returns int {
            var [name, score] = pair;
            io:println(io:sprintf("%s scored: %d", name, score));
            return score + 10;
        }
    );
    io:println(modifiedMarks);

    // A mapping constructor expression can also include a spread field,
    // referring to another mapping value. When a spread field is specified,
    // all the fields of the relevant mapping value are added to the new
    // mapping value being created.
    // A spread field is used with `modifiedMarks` to include all the entries
    // in `modifiedMarks` when creating `allMarks`.
    map<int> allMarks = {jane: 100, ...modifiedMarks, amy: 75};
    io:println(allMarks);
}
