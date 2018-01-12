function remove () (json) {
    json j = {"name":{"fname":"Jack", "lname":"Taylor"}, "state":"CA", "age":20};
    j.remove("name");
    return j;
}

function toString (json msg) (string) {
    return msg.toString();
}

function testParse (string jsonStr) (json, TypeConversionError) {
    var j, e = <json> jsonStr;
    return j, e;
}

function testGetKeys () (string[], string[], string[], string[]) {
    json j1 = {fname:"Jhon", lname:"Doe", age:40};
    json j2 = ["cat", "dog", "horse"];
    json j3 = "Hello";
    json j4 = 5;
    return j1.getKeys(), j2.getKeys(), j3.getKeys(), j4.getKeys();
}

function testToXML (json msg) (xml, TypeConversionError) {
    jsonOptions options = {};
    return msg.toXML(options);
}

function testToXMLStringValue () (xml, TypeConversionError) {
    jsonOptions options = {};
    json j = "value";
    return j.toXML(options);
}

function testToXMLBooleanValue () (xml, TypeConversionError) {
    jsonOptions options = {};
    json j = true;
    return j.toXML(options);
}

function testToXMLString (json msg) (string) {
    jsonOptions options = {};
    var xmlData, _ = msg.toXML(options);
    string s = <string> xmlData;
    return s;
}

function testToXMLWithXMLSequence (json msg) (string) {
    jsonOptions options = {};
    var xmlSequence, _ = msg.toXML(options);
    string s = <string> xmlSequence;
    return s;
}

function testToXMLWithOptions (json msg) (xml, TypeConversionError) {
    jsonOptions options = {attributePrefix:"#", arrayEntryTag:"wrapper"};
    return msg.toXML(options);
}

function testStringToJSONConversion() (json, TypeConversionError) {
    string s = "{\"foo\": \"bar\"}";
    var j, e = <json> s;
    return j, e;
}
