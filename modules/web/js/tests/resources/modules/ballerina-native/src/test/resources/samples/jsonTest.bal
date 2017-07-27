import ballerina.lang.jsons;
import ballerina.lang.xmls;

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
    jsons:addToObject(msg, jsonPath, key, value);
    return msg;
}

function addIntToObject(json msg, string jsonPath, string key, int value) (json) {
    jsons:addToObject(msg, jsonPath, key, value);
    return msg;
}

function addFloatToObject(json msg, string jsonPath, string key, float value) (json) {
    jsons:addToObject(msg, jsonPath, key, value);
    return msg;
}

function addBooleanToObject(json msg, string jsonPath, string key, boolean value) (json) {
    jsons:addToObject(msg, jsonPath, key, value);
    return msg;
}

function addElementToObject(json msg, string jsonPath, string key, json value) (json) {
    jsons:addToObject(msg, jsonPath, key, value);
    return msg;
}

function addStringToArray(json msg, string jsonPath, string value) (json){
    jsons:addToArray(msg, jsonPath, value);
    return msg;
}

function addIntToArray(json msg, string jsonPath, int value) (json){
    jsons:addToArray(msg, jsonPath, value);
    return msg;
}

function addFloatToArray(json msg, string jsonPath, float value) (json){
    jsons:addToArray(msg, jsonPath, value);
    return msg;
}

function addBooleanToArray(json msg, string jsonPath, boolean value) (json){
    jsons:addToArray(msg, jsonPath, value);
    return msg;
}

function addElementToArray(json msg, string jsonPath, json value) (json){
    jsons:addToArray(msg, jsonPath, value);
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

function testParse(string jsonStr) (json){
    return jsons:parse(jsonStr);
}

function testGetKeys() (string[], string[], string[], string[]){
    json j1 = {fname:"Jhon", lname:"Doe", age:40};
    json j2 = ["cat", "dog", "horse"];
    json j3 = "Hello";
    json j4 = 5;
    return jsons:getKeys(j1), jsons:getKeys(j2), jsons:getKeys(j3), jsons:getKeys(j4) ;
}

function testToXML(json msg) (xml){
    jsons:Options options = {};
    return jsons:toXML(msg, options);
}

function testToXMLStringValue() (xml){
    jsons:Options options = {};
    json j = "value";
    return jsons:toXML(j, options);
}

function testToXMLBooleanValue() (xml){
    jsons:Options options = {};
    json j = true;
    return jsons:toXML(j, options);
}

function testToXMLString(json msg) (string){
    jsons:Options options = {};
    xml xmlData = jsons:toXML(msg, options);
    return xmls:toString(xmlData);
}

function testToXMLWithXMLSequence(json msg) (string){
    jsons:Options options = {};
    xml xmlSequence = jsons:toXML(msg, options);
    return xmls:toString(xmlSequence);
}

function testToXMLWithOptions(json msg) (xml){
    jsons:Options options = {attributePrefix:"#", arrayEntryTag:"wrapper"};
    return jsons:toXML(msg, options);
}