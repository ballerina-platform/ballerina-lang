function remove () (json) {
    json j = {"name":{"fname":"Jack", "lname":"Taylor"}, "state":"CA", "age":20};
    j.remove("name");
    return j;
}

function toString (json msg) (string) {
    return msg.toString();
}

function testParse (string jsonStr) (json, error) {
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

function testToXML (json msg) (xml, error) {
    return msg.toXML({});
}

function testToXMLStringValue () (xml, error) {
    json j = "value";
    return j.toXML({});
}

function testToXMLBooleanValue () (xml, error) {
    json j = true;
    return j.toXML({});
}

function testToXMLString (json msg) (string) {
    var xmlData, _ = msg.toXML({});
    string s = <string> xmlData;
    return s;
}

function testToXMLWithXMLSequence (json msg) (string) {
    var xmlSequence, _ = msg.toXML({});
    string s = <string> xmlSequence;
    return s;
}

function testToXMLWithOptions (json msg) (xml, error) {
    return msg.toXML({attributePrefix:"#", arrayEntryTag:"wrapper"});
}

function testStringToJSONConversion() (json, error) {
    string s = "{\"foo\": \"bar\"}";
    var j, e = <json> s;
    return j, e;
}

function testJSONArrayToJsonAssignment() (json) {
    json[] j1 = [{"a":"b"}, {"c":"d"}];
    json j2 = j1;
    return j2;
}
