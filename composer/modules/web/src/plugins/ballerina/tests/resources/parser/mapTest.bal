import ballerina/lang.maps;

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
    string msg;
    int value;
    string[] keys;

    dataMap = { "country" : "US", "currency" : "Dollar" , "states" : 50};
    success = true;
    msg = "No Error found.!!!";

    value, _ = (int) dataMap["states"];

    // TODO : Fix dataMap["states"] != 50
    if(value != 50){
         success = false;
         msg = "Get operation failed.";
    }

    if(testLength(dataMap) != 3){
        success = false;
        msg = "length didn't match.";
    }

    keys = testGetKeys(dataMap);

    if(keys.length !=3){
        success = false;
        msg = "keys operation failed.";
    }

    testRemove(dataMap , "country");

    keys = testGetKeys(dataMap);

    if(keys.length != 2){
        success = false;
        msg = "remove operation failed.";
    }

    return success, msg;
}