import ballerina.lang.maps;
import ballerina.lang.arrays;

function testLength(map data)(int){
string[] str;
str = ["ss"];
    return maps:length(data);
}

function testGetKeys(map data)(string[]){
    return maps:keys(data);
}

function testRemove(map data, string key){
    maps:remove(data, key);
}

function testDefinition()(boolean, string){
    map dataMap;
    boolean success;
    string message;
    int value;
    string[] keys;

    dataMap = { "country" : "US", "currency" : "Dollar" , "states" : 50};
    success = true;
    message = "No Error found.!!!";

    value = dataMap["states"];

    // TODO : Fix dataMap["states"] != 50
    if(value != 50){
         success = false;
         message = "Get operation failed.";
    }

    if(testLength(dataMap) != 3){
        success = false;
        message = "length didn't match.";
    }

    keys = testGetKeys(dataMap);

    if(arrays:length(keys) !=3){
        success = false;
        message = "keys operation failed.";
    }

    testRemove(dataMap , "country");

    keys = testGetKeys(dataMap);

    if(arrays:length(keys) != 2){
        success = false;
        message = "remove operation failed.";
    }

    return success, message;
}