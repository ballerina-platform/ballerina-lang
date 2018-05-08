import ballerina/lang.system;
import ballerina/lang.maps;

function main (string... args) {
    //Here's how you create an empty map;
    map m = {};

    //Here's how you create a map with initial values;
    map addrMap = {line1:"No. 20", line2:"Palm Grove",
                      city:"Colombo 03", country:"Sri Lanka"};
    system:println(addrMap);

    //You can retrieve a value of a key using an index based notation as follows.
    var country, _ = (string)addrMap["country"];
    system:println(country);

    //Another way to retrieve a value from a map.
    var city, _ = (string)addrMap.city;
    system:println(city);

    //Here's how you can add or update the value of a key.
    addrMap["postalCode"] = "00300";
    addrMap.postalCode = "00301";
    system:println(addrMap);

    //You can use 'keys' function in the 'maps' package get an array keys.
    system:println(maps:keys(addrMap));

    //Number of keys in the map.
    system:println(maps:length(addrMap));

    //You can remove a key using the 'remove' method.
    maps:remove(addrMap, "postalCode");
    system:println(addrMap);
}