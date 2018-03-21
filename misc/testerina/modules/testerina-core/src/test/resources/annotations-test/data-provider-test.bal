import ballerina.test;
import ballerina.io;

@test:config{
    dataProvider:"dataGen"
}
function testFunc1 (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;
    io:println("Input params: ["+fValue+","+sValue+","+result+"]");
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
}

function dataGen() returns (string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

@test:config{
    dataProvider:"dataGen2"
}
function testFunc2 (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;
    io:println("Input params: ["+fValue+","+sValue+","+result+"]");
    test:assertEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:config{
    dataProvider:"dataGen3"
}
function testFunc3 (json fValue, json sValue, json result) {
    json a = {"a": "a"};
    json b = {"b": "b"};
    json c = {"c": "c"};
    test:assertEquals(fValue, a, msg = "json data provider failed");
    test:assertEquals(sValue, b, msg = "json data provider failed");
    test:assertEquals(result, c, msg = "json data provider failed");
}

function dataGen2() returns (string[][]) {
    return [["1", "2", "3"]];
}

function dataGen3() returns (json[][]) {
    return [[{"a": "a"}, {"b": "b"}, {"c": "c"}]];
}
