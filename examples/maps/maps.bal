import ballerina/io;

public function main() {
    // Declare a `map` constrained by type `any`.
    map<any> m;

    // You can also declare and initialize a map with a map literal.
    map<any> addrMap = { line1: "No. 20", line2: "Palm Grove",
                         city: "Colombo 03", country: "Sri Lanka" };
    io:println(addrMap);

    // Retrieve the value for a key using an index operator.
    string country = <string>addrMap["country"];
    io:println(country);

    // Retrieve the value for a key from the map using the dot (.) operator.
    string city = <string>addrMap.city;
    io:println(city);

    // Add or update the value for a key.
    addrMap["postalCode"] = "00300";
    addrMap.postalCode = "00301";
    io:println(addrMap);

    // Use the `keys()` function of the map to get the keys of the map as an array.
    io:println(addrMap.keys());

    // Print the number of keys in the map.
    io:println(addrMap.length());

    // Remove a key using the `remove()` function.
    boolean isRemoved = addrMap.remove("postalCode");
    io:println(addrMap);

    // Maps can only contain values of the type specified by the constraint type descriptor.
    map<string> stringMap = {};
    stringMap["index"] = "100892N";

    // Notice how you do not need explicit conversion to `string` when retrieving the value from map.
    string index = stringMap.index;
    io:println(index);

    // Note that the return type of index-based access will be `T?` (where T is the constraint type of the map`).
    // If the key does not exist, `nil` is returned.
    // Elvis operator `?:` is a conditional operator that handles `nil`. 
    // If the given expression evaluates to nil, the second expression is evaluated and its value is returned.
    string index2 = stringMap["index"] ?: "";
    io:println(index2);
}
