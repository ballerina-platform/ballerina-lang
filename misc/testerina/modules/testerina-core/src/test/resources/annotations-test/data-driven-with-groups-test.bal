import ballerina.test;

@test:config{
    valueSets:["1,2,3","4,4,8","1,5,7","4,4,5"],
    groups:["g1"]
}
function testFunc1 (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;

    test:assertIntEquals(value1 + value2, result1, "The sum is not correct");
}

@test:config{
    valueSets:["1,2,3","4,4,8","1,5,7","4,4,5"],
    groups:["g2"]
}
function testFunc2 (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;

    test:assertIntEquals(value1 + value2, result1, "The sum is not correct");
}

@test:config{
    valueSets:["1,2,3","4,4,2"],
    groups:["g2"]
}
function testFunc3 (string fValue, string sValue, string result) {

    var value1, _ = <int>fValue;
    var value2, _ = <int>sValue;
    var result1, _ = <int>result;

    test:assertIntEquals(value1 + value2, result1, "The sum is not correct");
}

@test:config{
    groups:["g3"]
}
function testFunc4 () {
    test:assertFalse(false, "Asset Failed.");
}

@test:config{}
function testFunc5 () {
    test:assertFalse(false, "Asset Failed.");
}