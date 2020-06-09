import ballerina/test;

// tests an invalid tuple data provider
@test:Config{
    dataProvider:"invalidDataGen3"
}
function testInvalidTupleDataProvider ([string, string, [string, string]] result) {

   // int|error fErr = trap result[0].cloneWithType(int);
   // int|error sErr = trap result[1].cloneWithType(int);
   // int|error resultErr = trap result.cloneWithType(int);
   test:assertTrue(true);
}

function invalidDataGen3() returns ([string, int, [int, int]][]) {
    return [["hi", 20, [30, 30]]];
}
