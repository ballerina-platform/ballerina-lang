import ballerina.test;
import ballerina.io;

@test:config{
    dataProvider:"ValueProvider"
}
function testAddingValues (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;
    io:println("Input params: ["+fValue+","+sValue+","+result+"]");
    test:assertEquals(value1 + value2, result1, "The sum is not correct");
}

// Data provider function
function ValueProvider()(string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

@test:config{
    dataProvider:"jsonDataProvider"
}
function testJsonObjects (json fValue, json sValue, json result) {
    json a = {"a": "a"};
    json b = {"b": "b"};
    json c = {"c": "c"};
    test:assertEquals(fValue, a, "json data provider failed");
    test:assertEquals(sValue, b, "json data provider failed");
    test:assertEquals(result, c, "json data provider failed");
}

function jsonDataProvider()(json[][]) {
    return [[{"a": "a"}, {"b": "b"}, {"c": "c"}]];
}
