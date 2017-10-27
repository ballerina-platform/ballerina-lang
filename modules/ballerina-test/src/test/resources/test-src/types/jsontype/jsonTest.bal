function remove () (json) {
    json j = {"name":{"fname":"Jack", "lname":"Taylor"}, "state":"CA", "age":20};
    j.remove("name");
    return j;
}

function toString (json msg) (string) {
    return msg.toString();
}

function testParse (string jsonStr) (json) {
    var j,_ = <json> jsonStr;
    return j;
}

function testGetKeys () (string[], string[], string[], string[]) {
    json j1 = {fname:"Jhon", lname:"Doe", age:40};
    json j2 = ["cat", "dog", "horse"];
    json j3 = "Hello";
    json j4 = 5;
    return j1.getKeys(), j2.getKeys(), j3.getKeys(), j4.getKeys();
}

function testToXML (json msg) (xml) {
    jsonOptions options = {};
    return msg.toXML(options);
}

function testToXMLStringValue () (xml) {
    jsonOptions options = {};
    json j = "value";
    return j.toXML(options);
}

function testToXMLBooleanValue () (xml) {
    jsonOptions options = {};
    json j = true;
    return j.toXML(options);
}

function testToXMLString (json msg) (string) {
    jsonOptions options = {};
    xml xmlData = msg.toXML(options);
    string s = <string> xmlData;
    return s;
}

function testToXMLWithXMLSequence (json msg) (string) {
    jsonOptions options = {};
    xml xmlSequence = msg.toXML(options);
    string s = <string> xmlSequence;
    return s;
}

function testToXMLWithOptions (json msg) (xml) {
    jsonOptions options = {attributePrefix:"#", arrayEntryTag:"wrapper"};
    return msg.toXML(options);
}
