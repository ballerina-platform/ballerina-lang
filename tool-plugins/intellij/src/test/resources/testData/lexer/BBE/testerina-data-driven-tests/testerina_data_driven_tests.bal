import ballerina/test;
import ballerina/io;

// The `dataProvider` attribute allows you to add a data provider function to the test-case. 
@test:Config {
    // `ValueProvider` provides the data set to this function.
    dataProvider: "ValueProvider"
}
// Data is parsed to the function as function parameters.
function testAddingValues(string fValue, string sValue, string result) {

    int value1 = check <int>fValue;
    int value2 = check <int>sValue;
    int result1 = check <int>result;
    io:println("Input : [" + fValue + "," + sValue + "," + result + "]");
    test:assertEquals(value1 + value2, result1, msg = "Incorrect Sum");
}

// The data provider function. In this scenario, it returns a string value-set. 
function ValueProvider() returns (string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

// This is the test function. Here we provide a JSON value set as the dataset.
@test:Config {
    dataProvider: "jsonDataProvider"
}
function testJsonObjects(json fValue, json sValue, json result) {
    json a = { "a": "a" };
    json b = { "b": "b" };
    json c = { "c": "c" };
    test:assertEquals(fValue, a, msg = "json data provider failed");
    test:assertEquals(sValue, b, msg = "json data provider failed");
    test:assertEquals(result, c, msg = "json data provider failed");
}

// This function returns a JSON value set.
function jsonDataProvider() returns (json[][]) {
    return [[{ "a": "a" }, { "b": "b" }, { "c": "c" }]];
}
