import ballerina/test;
import ballerina/io;

// dataProvider attribute allows you to add a data provider function to the test-case
@test:Config{
    dataProvider:"ValueProvider"
}
function testAddingValues (string fValue, string sValue, string result) {

    int value1 = check <int>fValue;
    int value2 = check <int>sValue;
    int result1 = check <int>result;
    io:println("Input params: ["+fValue+","+sValue+","+result+"]");
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
}

// Data provider function which provides string value-set
function ValueProvider() returns (string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

// Providing Json objects as a value set
@test:Config{
    dataProvider:"jsonDataProvider"
}
function testJsonObjects (json fValue, json sValue, json result) {
    json a = {"a": "a"};
    json b = {"b": "b"};
    json c = {"c": "c"};
    test:assertEquals(fValue, a, msg = "json data provider failed");
    test:assertEquals(sValue, b, msg = "json data provider failed");
    test:assertEquals(result, c, msg = "json data provider failed");
}

// This function returns a Json value set
function jsonDataProvider() returns (json[][]) {
    return [[{"a": "a"}, {"b": "b"}, {"c": "c"}]];
}
