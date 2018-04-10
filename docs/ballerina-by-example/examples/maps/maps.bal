import ballerina/io;

function main (string[] args) {
    //Here's how you create an empty map.
    map m;

    //Here's how you create a map with values.
    map addrMap = {line1:"No. 20", line2:"Palm Grove",
                      city:"Colombo 03", country:"Sri Lanka"};
    io:println(addrMap);

    //Here's how you can retrieve a value of a key using an index based notation.
    var country = <string>addrMap["country"];
    io:println(country);

    //Here's another way to retrieve a value of a key from the map.
    var city = <string>addrMap.city;
    io:println(city);

    //Here's how you can add or update the value of a key.
    addrMap["postalCode"] = "00300";
    addrMap.postalCode = "00301";
    io:println(addrMap);

    //You can use the 'keys' function in the 'maps' package to get an array of keys.
    io:println(addrMap.keys());

    //Print the number of keys in the map.
    io:println(lengthof addrMap);

    //You can remove a key using the 'remove' method.
    var isRemoved = addrMap.remove("postalCode");
    io:println(addrMap);
}
