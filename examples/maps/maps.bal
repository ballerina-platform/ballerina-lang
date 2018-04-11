import ballerina/io;

function main (string[] args) {
    //Here's how you create an empty map.
    map m;

    //Here's how you create a map with initial values.
    map addrMap = {line1:"No. 20", line2:"Palm Grove",
                      city:"Colombo 03", country:"Sri Lanka"};
    io:println(addrMap);

    //You can retrieve a value of a key using an index based notation as follows.
    var country = <string>addrMap["country"];
    io:println(country);

    //Another way to retrieve a value from a map.
    var city = <string>addrMap.city;
    io:println(city);

    //Here's how you can add or update the value of a key.
    addrMap["postalCode"] = "00300";
    addrMap.postalCode = "00301";
    io:println(addrMap);

    //You can use 'keys' function in the 'maps' package get an array keys.
    io:println(addrMap.keys());

    //Number of keys in the map.
    io:println(lengthof addrMap);

    //You can remove a key using the 'remove' method.
    var isRemoved = addrMap.remove("postalCode");
    io:println(addrMap);
}
