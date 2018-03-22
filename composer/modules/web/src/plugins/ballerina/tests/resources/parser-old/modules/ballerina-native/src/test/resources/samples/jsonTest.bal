import ballerina/lang.jsons;
import ballerina/lang.xmls;

function remove() (json){
    json j = {"name":{"fname":"Jack","lname":"Taylor"}, "state":"CA", "age":20};
    jsons:remove(j, "name");
    return j;
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