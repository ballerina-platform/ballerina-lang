import ballerina/io;

public function main() {
    // Declare a `map` constrained by the type `any`.
    map<any> m;

    // You can also declare and initialize a `map` with a map literal.
    map<any> addrMap = { line1: "No. 20", line2: "Palm Grove",
        city: "Colombo 03", country: "Sri Lanka" };
    io:println(addrMap);

    // Retrieve a value using index-based access.
    string country = <string>addrMap["country"];
    io:println(country);

    // Retrieve a value using field-based access.
    string city = <string>addrMap.city;
    io:println(city);

    // Add or update the value of a key.
    addrMap["postalCode"] = "00300";
    addrMap.postalCode = "00301";
    io:println(addrMap);

    // The `keys()` method returns the keys of the map as an array.
    io:println(addrMap.keys());

    // Print the number of mappings in the `map`.
    io:println(addrMap.length());

    // Mappings can be removed using the `remove()` method.
    boolean isRemoved = addrMap.remove("postalCode");
    io:println(addrMap);

    // Maps can only contain values of the type specified by the constraint type descriptor.
    map<string> stringMap = {};
    stringMap["index"] = "100892N";

    // You do not need explicit conversion to `string` when retrieving a value from `stringMap` via field-based access.
    string index = stringMap.index;
    io:println(index);

    // Note that the return type of index-based access will be `T?` (where T is the constraint type of the map`).
    // If the key does not exist, `nil` is returned.
    // Elvis operator `?:` is a conditional operator that handles `nil`.
    // If the given expression evaluates to nil, the second expression is evaluated and its value is returned.
    string index2 = stringMap["index"] ?: "";
    io:println(index2);
}
