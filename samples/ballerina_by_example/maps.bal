import ballerina.lang.system;
import ballerina.lang.map;
import ballerina.lang.array;

function main (string[] args) {

    //Creating an empty map.
    map dataMap;

    string value;
    string[] keys;

    //Initializing a map.
    dataMap = { "country" : "US", "currency" : "Dollar" , "states" : "50"};

    //Obtaining a value corresponding to a key.
    value = dataMap["states"];
    system:println(value);

    //Printing the length of the map.
    system:println(map:length(dataMap));

    //Getting an array containing the set of keys.
    keys = map:keys(dataMap);

    //Printing the length of the array.
    system:println(array:length(keys));

    //Removing a key/value pair from the map.
    map:remove(dataMap, "country");

    //Printing the new length of the map.
    system:println(map:length(dataMap));
}