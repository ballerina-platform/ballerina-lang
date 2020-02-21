import ballerina/test;

// tests an invalid data provider
@test:Config{
    dataProvider:"invalidDataGen2"
}
function testInvalidDataProvider2(string fValue, string sValue, string result) {

    int|error fErr = trap int.constructFrom(fValue);
    int|error sErr = trap int.constructFrom(sValue);
    int|error resultErr = trap int.constructFrom(result);
}

function invalidDataGen2() returns (int[][]) {
    return [[1, 2, 3]];
}
