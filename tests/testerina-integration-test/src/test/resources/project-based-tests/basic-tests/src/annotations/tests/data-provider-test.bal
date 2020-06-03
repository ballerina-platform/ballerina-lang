import ballerina/test;
import ballerina/io;

@test:Config{
    dataProvider:"dataGen"
}
function stringDataProviderTest (string fValue, string sValue, string result) returns error? {

    int|error val1 = fValue.cloneWithType(int);
    int value1 = val1 is int ? val1 : 0;
    int|error val2 = sValue.cloneWithType(int);
    var value2 = val2 is int ? val2 : 0;
    int|error res1 = result.cloneWithType(int);
    var result1 = res1 is int ? res1 : 0;
    io:println("Input params: ["+fValue+","+sValue+","+result+"]");
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
    return;
}

function dataGen() returns (string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

@test:Config{
    dataProvider:"dataGen2"
}
function stringDataProviderTest2 (string fValue, string sValue, string result) returns error? {
            
    int|error val1 = fValue.cloneWithType(int);
    int value1 = val1 is int ? val1 : 0;
    int|error val2 = sValue.cloneWithType(int);
    var value2 = val2 is int ? val2 : 0;
    int|error res1 = result.cloneWithType(int);
    var result1 = res1 is int ? res1 : 0;
    io:println("Input params: ["+fValue+","+sValue+","+result+"]");
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
    return;
}

function dataGen2() returns (string[][]) {
    return [["1", "2", "3"]];
}

@test:Config{
    dataProvider:"dataGen3"
}
function jsonDataProviderTest (json fValue, json sValue, json result) {
    json a = {"a": "a"};
    json b = {"b": "b"};
    json c = {"c": "c"};
    test:assertEquals(fValue, a, msg = "json data provider failed");
    test:assertEquals(sValue, b, msg = "json data provider failed");
    test:assertEquals(result, c, msg = "json data provider failed");
}

function dataGen3() returns (json[][]) {
    return [[{"a": "a"}, {"b": "b"}, {"c": "c"}]];
}

@test:Config{
    dataProvider:"dataGen4"
}
function tupleDataProviderTest ([int, int, [int, int]] result) {
    int a = 10;
    int b = 20;
    [int, int] c = [30, 30];
    test:assertEquals(result[0], a, msg = "tuple data provider failed");
    test:assertEquals(result[1], b, msg = "tuple data provider failed");
    test:assertEquals(result[2], c, msg = "tuple data provider failed");
}

function dataGen4() returns ([int, int, [int, int]][]) {
    return [[10, 20, [30, 30]]];
}
