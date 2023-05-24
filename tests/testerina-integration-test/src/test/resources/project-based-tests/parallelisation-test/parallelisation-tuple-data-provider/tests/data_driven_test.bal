import ballerina/lang.runtime;
import ballerina/test;

@test:Config {
    dataProvider: stringDataProvider
}
function testAddingValues0(string fValue, string sValue, string result) returns error? {
    int value1 = check 'int:fromString(fValue);
    int value2 = check 'int:fromString(sValue);
    int result1 = check 'int:fromString(result);
    runtime:sleep(1);
    test:assertEquals(value1 + value2, result1, msg = "Incorrect Sum");
}

function stringDataProvider() returns (string[][]) {
    return [
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"]
    ,
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"]
    ,
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"],
        ["1", "2", "3"],
        ["10", "20", "30"],
        ["5", "6", "11"]
    ];
}
