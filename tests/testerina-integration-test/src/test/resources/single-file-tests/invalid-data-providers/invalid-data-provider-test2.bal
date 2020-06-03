import ballerina/test;

// tests an invalid data provider
@test:Config{
    dataProvider:"invalidDataGen2"
}
function testInvalidDataProvider2(string fValue, string sValue, string result) {

    int|error fErr = trap fValue.cloneWithType(int);
    int|error sErr = trap sValue.cloneWithType(int);
    int|error resultErr = trap result.cloneWithType(int);
}

function invalidDataGen2() returns (int[][]) {
    return [[1, 2, 3]];
}
