import ballerina/test;

@test:Config{
    valueSets:["1,2,3","4,4,8","1,5,7","4,4,5"],
    groups:["g1"]
}
function testFunc1 (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;

    test:assertIntEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:Config{
    valueSets:["1,2,3","4,4,8","1,5,7","4,4,5"],
    groups:["g2"]
}
function testFunc2 (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;

    test:assertIntEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:Config{
    valueSets:["1,2,3","4,4,2"],
    groups:["g2"]
}
function testFunc3 (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;

    test:assertIntEquals(value1 + value2, result1, msg = "The sum is not correct");
}

@test:Config{
    groups:["g3"]
}
function testFunc4 () {
    test:assertFalse(false, msg = "Asset Failed.");
}

@test:Config{}
function testFunc5 () {
    test:assertFalse(false, msg = "Asset Failed.");
}