import ballerina.lang.jsons;
function getString(json msg, string jsonPath) (string){
    return jsons:getString(msg, jsonPath);
}

function getInt(json msg, string jsonPath) (int){
    return jsons:getInt(msg, jsonPath);
}

function getJson(json msg, string jsonPath) (json){
    return jsons:getJson(msg, jsonPath);
}

function getFloat(json msg, string jsonPath) (float){
    return jsons:getFloat(msg, jsonPath);
}

function getDouble(json msg, string jsonPath) (double){
    return jsons:getDouble(msg, jsonPath);
}

function getBoolean(json msg, string jsonPath) (boolean){
    return jsons:getBoolean(msg, jsonPath);
}


function setString(json msg, string jsonPath, string value) (string) {
    jsons:set(msg, jsonPath, value);
    return jsons:getString(msg, jsonPath);
}

function setInt(json msg, string jsonPath, int value) (int) {
    jsons:set(msg, jsonPath, value);
    return jsons:getInt(msg, jsonPath);
}

function setDouble(json msg, string jsonPath, double value) (double) {
    jsons:set(msg, jsonPath, value);
    return jsons:getDouble(msg, jsonPath);
}

function setFloat(json msg, string jsonPath, float value) (float) {
    jsons:set(msg, jsonPath, value);
    return jsons:getFloat(msg, jsonPath);
}

function setBoolean(json msg, string jsonPath, boolean value) (boolean) {
    jsons:set(msg , jsonPath , value);
    return jsons:getBoolean(msg, jsonPath);
}

function setJson(json msg, string jsonPath, json value) (json) {
    jsons:set(msg, jsonPath, value);
    return jsons:getJson(msg, jsonPath);
}


function addStringToObject(json msg, string jsonPath, string key, string value) (json) {
    jsons:add(msg, jsonPath, key, value);
    return msg;
}

function addIntToObject(json msg, string jsonPath, string key, int value) (json) {
    jsons:add(msg, jsonPath, key, value);
    return msg;
}

function addDoubleToObject(json msg, string jsonPath, string key, double value) (json) {
    jsons:add(msg, jsonPath, key, value);
    return msg;
}

function addFloatToObject(json msg, string jsonPath, string key, float value) (json) {
    jsons:add(msg, jsonPath, key, value);
    return msg;
}

function addBooleanToObject(json msg, string jsonPath, string key, boolean value) (json) {
    jsons:add(msg, jsonPath, key, value);
    return msg;
}

function addElementToObject(json msg, string jsonPath, string key, json value) (json) {
    jsons:add(msg, jsonPath, key, value);
    return msg;
}


function addStringToArray(json msg, string jsonPath, string value) (json){
    jsons:add(msg, jsonPath, value);
    return msg;
}

function addIntToArray(json msg, string jsonPath, int value) (json){
    jsons:add(msg, jsonPath, value);
    return msg;
}

function addDoubleToArray(json msg, string jsonPath, double value) (json){
    jsons:add(msg, jsonPath, value);
    return msg;
}

function addFloatToArray(json msg, string jsonPath, float value) (json){
    jsons:add(msg, jsonPath, value);
    return msg;
}

function addBooleanToArray(json msg, string jsonPath, boolean value) (json){
    jsons:add(msg, jsonPath, value);
    return msg;
}

function addElementToArray(json msg, string jsonPath, json value) (json){
    jsons:add(msg, jsonPath, value);
    return msg;
}



function remove(json msg, string jsonPath) (json){
    jsons:remove(msg, jsonPath);
    return msg;
}

function rename(json msg, string jsonPath, string oldKey, string newKey) (string){
    jsons:rename(msg, jsonPath, oldKey, newKey);
    jsonPath = "$.name.firstName";
    return jsons:getString(msg, jsonPath);
}

function toString(json msg) (string){
    return jsons:toString(msg);
}