import ballerina/io;

function main(string... args) {
    // The implicit initial value of a map is the empty map.
    map m;

    // You can also declare and initialize a map with a map literal.
    map addrMap = { line1: "No. 20", line2: "Palm Grove", city: "Colombo 03", country: "Sri Lanka" };
    io:println(addrMap);

    // This retrieves a value of a key using an index operator.
    var country = <string>addrMap["country"];
    io:println(country);

    // This retrieves a value of a key from the map using the dot (.) operator.
    var city = <string>addrMap.city;
    io:println(city);

    // This adds or updates the value of a key.
    addrMap["postalCode"] = "00300";
    addrMap.postalCode = "00301";
    io:println(addrMap);

    // You can use the `keys()` function of the map to get the keys of the map as an array.
    io:println(addrMap.keys());

    // Print the number of keys in the map.
    io:println(lengthof addrMap);

    // You can remove a key using the `remove()` function.
    var isRemoved = addrMap.remove("postalCode");
    io:println(addrMap);

    // Constrained maps can only contain values of the type specified by the type descriptor.
    map<string> stringMap;

    // There is no difference in how a value is added or updated in a constrained map
    stringMap["index"] = "100892N";

    // Notice you do not need explicit conversion to string here when retrieving value from map.
    string index = stringMap.index;
    io:println(index);

    // Note that Index-based access will return a union of constraint+nil.
    // If the key does not exist a nil value is returned.
    string index2 = stringMap["index"] ?: "";
    io:println(index2);
}
