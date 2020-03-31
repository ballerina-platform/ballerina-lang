import ballerina/io;
import ballerina/test;

// The `dataProvider` attribute allows you to add a data provider function to the test-case.
@test:Config {
    // The `stringDataProvider` function provides the data set to this function.
    dataProvider: "stringDataProvider"
}
// Data is passed to the function as function parameters.
function testAddingValues(string fValue, string sValue, string result) {

    int|error val1 = int.constructFrom(fValue);
    int value1 = val1 is int ? val1 : 0;
    int|error val2 = int.constructFrom(sValue);
    int value2 = val2 is int ? val2 : 0;
    int|error res1 = int.constructFrom(result);
    int result1 = res1 is int ? res1 : 0;

    io:println("Input : [" + fValue + "," + sValue + "," + result + "]");
    test:assertEquals(value1 + value2, result1, msg = "Incorrect Sum");
}

// The data provider function, which returns a `string` value-set.
function stringDataProvider() returns (string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

@test:Config {
    // The `jsonDataProvider` function provides the data set to this function.
    dataProvider: "jsonDataProvider"
}
function testJsonObjects(json fValue, json sValue, json result) {
    json a = {"a": "a"};
    json b = {"b": "b"};
    json c = {"c": "c"};
    test:assertEquals(fValue, a, msg = "json data provider failed");
    test:assertEquals(sValue, b, msg = "json data provider failed");
    test:assertEquals(result, c, msg = "json data provider failed");
}

// The data provider function, which returns a JSON value-set.
function jsonDataProvider() returns (json[][]) {
    return [[{"a": "a"}, {"b": "b"}, {"c": "c"}]];
}
