import ballerina/io;
import ballerina/internal;

function remove () returns (json) {
    json j = {"name":{"fname":"Jack", "lname":"Taylor"}, "state":"CA", "age":20};
    map<json> jm = <map<json>> j;
    _ = jm.remove("name");
    return j;
}

function toString (json msg) returns (string?) {
    return msg.toString();
}

function testParse (string jsonStr) returns @tainted (json | error) {
    io:StringReader reader = new(jsonStr);
    return reader.readJson();
}

function testGetKeys () returns (string[]) {
    json j = {fname:"Jhon", lname:"Doe", age:40};
    map<json> jm = <map<json>> j;
    return jm.keys();
}

function testStringToJSONConversion() returns @tainted (json | error) {
    string s = "{\"foo\": \"bar\"}";
    io:StringReader reader = new(s);
    return reader.readJson();
}

function testJSONArrayToJsonAssignment() returns (json) {
    json[] j1 = [{"a":"b"}, {"c":"d"}];
    json j2 = j1;
    return j2;
}
