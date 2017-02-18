import ballerina.lang.jsonutils;
function getString(json msg, string jsonPath) (string){
    return jsonutils:getString(msg, jsonPath);
}

function getInt(json msg, string jsonPath) (int){
    return jsonutils:getInt(msg, jsonPath);
}

function getJson(json msg, string jsonPath) (json){
    return jsonutils:getJson(msg, jsonPath);
}

function getFloat(json msg, string jsonPath) (float){
    return jsonutils:getFloat(msg, jsonPath);
}

function getDouble(json msg, string jsonPath) (double){
    return jsonutils:getDouble(msg, jsonPath);
}

function getBoolean(json msg, string jsonPath) (boolean){
    return jsonutils:getBoolean(msg, jsonPath);
}


function setString(json msg, string jsonPath, string value) (string) {
    jsonutils:set(msg, jsonPath, value);
    return jsonutils:getString(msg, jsonPath);
}

function setInt(json msg, string jsonPath, int value) (int) {
    jsonutils:set(msg, jsonPath, value);
    return jsonutils:getInt(msg, jsonPath);
}

function setDouble(json msg, string jsonPath, double value) (double) {
    jsonutils:set(msg, jsonPath, value);
    return jsonutils:getDouble(msg, jsonPath);
}

function setFloat(json msg, string jsonPath, float value) (float) {
    jsonutils:set(msg, jsonPath, value);
    return jsonutils:getFloat(msg, jsonPath);
}

function setBoolean(json msg, string jsonPath, boolean value) (boolean) {
    jsonutils:set(msg , jsonPath , value);
    return jsonutils:getBoolean(msg, jsonPath);
}

function setJson(json msg, string jsonPath, json value) (json) {
    jsonutils:set(msg, jsonPath, value);
    return jsonutils:getJson(msg, jsonPath);
}


function addStringToObject(json msg, string jsonPath, string key, string value) (json) {
    jsonutils:add(msg, jsonPath, key, value);
    return msg;
}

function addIntToObject(json msg, string jsonPath, string key, int value) (json) {
    jsonutils:add(msg, jsonPath, key, value);
    return msg;
}

function addDoubleToObject(json msg, string jsonPath, string key, double value) (json) {
    jsonutils:add(msg, jsonPath, key, value);
    return msg;
}

function addFloatToObject(json msg, string jsonPath, string key, float value) (json) {
    jsonutils:add(msg, jsonPath, key, value);
    return msg;
}

function addBooleanToObject(json msg, string jsonPath, string key, boolean value) (json) {
    jsonutils:add(msg, jsonPath, key, value);
    return msg;
}

function addElementToObject(json msg, string jsonPath, string key, json value) (json) {
    jsonutils:add(msg, jsonPath, key, value);
    return msg;
}


function addStringToArray(json msg, string jsonPath, string value) (json){
    jsonutils:add(msg, jsonPath, value);
    return msg;
}

function addIntToArray(json msg, string jsonPath, int value) (json){
    jsonutils:add(msg, jsonPath, value);
    return msg;
}

function addDoubleToArray(json msg, string jsonPath, double value) (json){
    jsonutils:add(msg, jsonPath, value);
    return msg;
}

function addFloatToArray(json msg, string jsonPath, float value) (json){
    jsonutils:add(msg, jsonPath, value);
    return msg;
}

function addBooleanToArray(json msg, string jsonPath, boolean value) (json){
    jsonutils:add(msg, jsonPath, value);
    return msg;
}

function addElementToArray(json msg, string jsonPath, json value) (json){
    jsonutils:add(msg, jsonPath, value);
    return msg;
}



function remove(json msg, string jsonPath) (json){
    jsonutils:remove(msg, jsonPath);
    return msg;
}

function rename(json msg, string jsonPath, string oldKey, string newKey) (string){
    jsonutils:rename(msg, jsonPath, oldKey, newKey);
    jsonPath = "$.name.firstName";
    return jsonutils:getString(msg, jsonPath);
}

function toString(json msg) (string){
    return jsonutils:toString(msg);
}