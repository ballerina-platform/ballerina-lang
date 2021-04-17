import ballerina/test;

function stringDataProvider() returns (string[][]) {
    return [["1", "2", "3"], ["10", "20", "30"], ["5", "6", "11"]];
}

@test:Config {dataProvider: stringDataProvider}
function testAddingValues(string fValue, string sValue, string result) {
    int|error val1 = 'int:fromString(fValue);
    int value1 = val1 is int ? val1 : 0;
    int|error val2 = 'int:fromString(sValue);
    int value2 = val2 is int ? val2 : 0;
    int|error res1 = 'int:fromString(result);
    int result1 = res1 is int ? res1 : 0;
    test:assertEquals(value1 + value2, result1, msg = "Incorrect Sum");
}

@test:Config {dependsOn: [testAddingValues]}
function testAddingValuesDependant() {
    test:assertTrue(true, msg = "Failed!");
}
