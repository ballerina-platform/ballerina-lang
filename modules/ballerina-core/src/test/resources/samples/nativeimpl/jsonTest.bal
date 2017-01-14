// Get Functions
function getString(json msg, string jsonPath) (string){
    return ballerina.lang.json:getString(msg, jsonPath);
}

function getInt(json msg, string jsonPath) (int){
    return ballerina.lang.json:getInt(msg, jsonPath);
}

function getJson(json msg, string jsonPath) (json){
    return ballerina.lang.json:getJson(msg, jsonPath);
}

function getFloat(json msg, string jsonPath) (float){
    return ballerina.lang.json:getFloat(msg, jsonPath);
}

function getDouble(json msg, string jsonPath) (double){
    return ballerina.lang.json:getDouble(msg, jsonPath);
}

function getBoolean(json msg, string jsonPath) (boolean){
    return ballerina.lang.json:getBoolean(msg, jsonPath);
}


// Set Functions
function setString(json msg, string jsonPath, string value) (string) {
    ballerina.lang.json:set(msg, jsonPath, value);
    return ballerina.lang.json:getString(msg, jsonPath);
}

function setInt(json msg, string jsonPath, int value) (int) {
    ballerina.lang.json:set(msg, jsonPath, value);
    return ballerina.lang.json:getInt(msg, jsonPath);
}

function setDouble(json msg, string jsonPath, double value) (double) {
    ballerina.lang.json:set(msg, jsonPath, value);
    return ballerina.lang.json:getDouble(msg, jsonPath);
}

function setFloat(json msg, string jsonPath, float value) (float) {
    ballerina.lang.json:set(msg, jsonPath, value);
    return ballerina.lang.json:getFloat(msg, jsonPath);
}

function setBoolean(json msg, string jsonPath, boolean value) (boolean) {
    ballerina.lang.json:set(msg , jsonPath , value);
    return ballerina.lang.json:getBoolean(msg, jsonPath);
}

function setJson(json msg, string jsonPath, json value) (json) {
    ballerina.lang.json:set(msg, jsonPath, value);
    return ballerina.lang.json:getJson(msg, jsonPath);
}


// Add to object functions
function addStringToObject(json msg, string jsonPath, string key, string value) (json) {
    ballerina.lang.json:add(msg, jsonPath, key, value);
    return msg;
}

function addIntToObject(json msg, string jsonPath, string key, int value) (json) {
    ballerina.lang.json:add(msg, jsonPath, key, value);
    return msg;
}

function addDoubleToObject(json msg, string jsonPath, string key, double value) (json) {
    ballerina.lang.json:add(msg, jsonPath, key, value);
    return msg;
}

function addFloatToObject(json msg, string jsonPath, string key, float value) (json) {
    ballerina.lang.json:add(msg, jsonPath, key, value);
    return msg;
}

function addBooleanToObject(json msg, string jsonPath, string key, boolean value) (json) {
    ballerina.lang.json:add(msg, jsonPath, key, value);
    return msg;
}

function addElementToObject(json msg, string jsonPath, string key, json value) (json) {
    ballerina.lang.json:add(msg, jsonPath, key, value);
    return msg;
}


// Add to array functions
function addStringToArray(json msg, string jsonPath, string value) (json){
    ballerina.lang.json:add(msg, jsonPath, value);
    return msg;
}

function addIntToArray(json msg, string jsonPath, int value) (json){
    ballerina.lang.json:add(msg, jsonPath, value);
    return msg;
}

function addDoubleToArray(json msg, string jsonPath, double value) (json){
    ballerina.lang.json:add(msg, jsonPath, value);
    return msg;
}

function addFloatToArray(json msg, string jsonPath, float value) (json){
    ballerina.lang.json:add(msg, jsonPath, value);
    return msg;
}

function addBooleanToArray(json msg, string jsonPath, boolean value) (json){
    ballerina.lang.json:add(msg, jsonPath, value);
    return msg;
}

function addElementToArray(json msg, string jsonPath, json value) (json){
    ballerina.lang.json:add(msg, jsonPath, value);
    return msg;
}



// Remove Function
function remove(json msg, string jsonPath) (json){
    ballerina.lang.json:remove(msg, jsonPath);
    return msg;
}

// Rename Function
function rename(json msg, string jsonPath, string oldKey, string newKey) (string){
    ballerina.lang.json:rename(msg, jsonPath, oldKey, newKey);
    jsonPath = "$.name.firstName";
    return ballerina.lang.json:getString(msg, jsonPath);
}

// Rename Function
function toString(json msg) (string){
    return ballerina.lang.json:toString(msg);
}